package gitlet;


import static gitlet.Utils.*;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author XIAOBU
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0){
            throw error("Must have at least one argument");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkArgs(args, 1); Repository.init();
                break;
            case "add":
                checkArgs(args, 2); Repository.add(args[1]);
                break;
            case "commit":
                checkArgs(args, 2); Repository.commit(args[1]);
                break;
            case "rm":
                checkArgs(args, 2); Repository.rm(args[1]);
                break;
            case "log":
                checkArgs(args, 1); Repository.log();
                break;
            case "global-log":
                checkArgs(args, 1); Repository.globalLog();
                break;
            case "find":
                checkArgs(args, 2); Repository.find(args[1]);
                break;
            case "status":
                checkArgs(args, 1); Repository.status();
                break;
            case "checkout":
                Repository.checkout(args);
                break;   // 参数最复杂，自己解析
            case "branch":
                checkArgs(args, 2); Repository.branch(args[1]);
                break;
            case "rm-branch":
                checkArgs(args, 2); Repository.rmBranch(args[1]);
                break;
            case "reset":
                checkArgs(args, 2); Repository.reset(args[1]);
                break;
            case "merge":
                checkArgs(args, 2); Repository.merge(args[1]);
                break;
            default:
                throw error("No command with that name exists.");
        }
    }

    private static void checkArgs(String[] args, int n) {
        if (args.length != n) {
            throw error("Incorrect operands.");
        }
    }
}
