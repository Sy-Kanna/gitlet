package gitlet;


/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Kanna Shan
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */


    public static void main(String[] args) {
        // args is empty
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        Repository repo = new Repository();
        switch (firstArg) {
            case "init":
                // handle the `init` command
                checkArgs(args, 1);
                repo.init();
                break;
            case "add":
                // handle the `add [filename]` command
                checkArgs(args, 2);
                repo.restore();
                repo.add(args[1]);
                break;
            case "commit":
                checkArgs(args, 2);
                repo.restore();
                repo.commit(args[1]);
                break;
            case "rm":
                checkArgs(args, 2);
                repo.restore();
                repo.rm(args[1]);
                break;
            case "log":
                checkArgs(args, 1);
                repo.restore();
                repo.log();
                break;
            case "global-log":
                checkArgs(args, 1);
                repo.restore();
                repo.globalLog();
                break;
            case "find":
                checkArgs(args, 2);
                repo.restore();
                repo.find(args[1]);
                break;
            case "status":
                checkArgs(args, 1);
                repo.restore();
                repo.status();
                break;
            case "checkout":
                repo.restore();
                checkoutSwitch(args, repo);
                break;
            case "branch":
                checkArgs(args, 2);
                repo.restore();
                repo.branch(args[1]);
                break;
            case "rm-branch":
                checkArgs(args, 2);
                repo.restore();
                repo.rmBranch(args[1]);
                break;
            case "reset":
                checkArgs(args, 2);
                repo.restore();
                repo.reset(args[1]);
                break;
            case "merge":
                checkArgs(args, 2);
                repo.restore();
                repo.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

    private static void checkArgs(String[] args, int num) {
        if (args.length < num) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        if (args.length > num) {
            System.out.println("Invalid operands.");
        }
    }

    private static void checkoutSwitch(String[] args, Repository repo) {
        if (args.length == 1) {
            System.out.println("Please enter a command.");
        } else if (args.length == 2) {
            repo.checkoutBranch(args[1]);
        } else if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            repo.checkoutFile(args[2]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            repo.checkoutCommit(args[1], args[3]);
        } else {
            System.out.println("Invalid operands.");
        }
    }
}
