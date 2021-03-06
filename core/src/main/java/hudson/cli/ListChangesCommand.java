package hudson.cli;

import hudson.Extension;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.QuotedStringTokenizer;
import jenkins.scm.RunWithSCM;
import org.kohsuke.args4j.Option;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Retrieves a change list for the specified builds.
 *
 * @author Kohsuke Kawaguchi
 */

@Restricted(NoExternalUse.class) // command implementation only
public abstract class ListChangesCommand extends RunRangeCommand {
    public String getShortDescription() {
        return Messages.ListChangesCommand_ShortDescription();
    }

//    @Override
//    protected void printUsageSummary(PrintStream stderr) {
//        TODO
//    }
    public abstract int act(List<Run<?, ?>> builds);
}

class XML extends ListChangesCommand{
  @Override
  public int act(List<Run<?, ?>> builds){
    try{
      PrintWriter w = new PrintWriter(stdout);
      w.println("<changes>");
      for (Run<?, ?> build : builds) {
          if (build instanceof RunWithSCM) {
              w.println("<build number='" + build.getNumber() + "'>");
              for (ChangeLogSet<?> cs : ((RunWithSCM<?, ?>) build).getChangeSets()) {
                  Model p = new ModelBuilder().get(cs.getClass());
                  p.writeTo(cs, Flavor.XML.createDataWriter(cs, w));
              }
              w.println("</build>");
          }
      }
      w.println("</changes>");
      w.flush();
    } catch (IOException ex){
    }
    return 0;
  }
}

class CSV extends ListChangesCommand{
  @Override
  public int act(List<Run<?, ?>> builds){
      for (Run<?, ?> build : builds) {
        if (build instanceof RunWithSCM) {
            for (ChangeLogSet<?> cs : ((RunWithSCM<?, ?>) build).getChangeSets()) {
                for (Entry e : cs) {
                    stdout.printf("%s,%s%n",
                            QuotedStringTokenizer.quote(e.getAuthor().getId()),
                            QuotedStringTokenizer.quote(e.getMsg()));
                }
            }
        }
      }
    return 0;
  }
}

class PLAIN extends ListChangesCommand{
  @Override
  public int act(List<Run<?, ?>> builds){
      for (Run<?, ?> build : builds) {
          if (build instanceof RunWithSCM) {
              for (ChangeLogSet<?> cs : ((RunWithSCM<?, ?>) build).getChangeSets()) {
                  for (Entry e : cs) {
                      stdout.printf("%s\t%s%n", e.getAuthor(), e.getMsg());
                      for (String p : e.getAffectedPaths()) {
                          stdout.println("  " + p);
                      }
                  }
              }
          }
      }
    return 0;
  }
}
