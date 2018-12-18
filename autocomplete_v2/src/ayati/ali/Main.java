package ayati.ali;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private static String path;
    private static SuggestTree tree = new SuggestTree(100);

    public static void main(String[] args){

        start();
    }

    private static void read_file_and_build_tree(
    ){
        BufferedReader br = null;
        FileReader fr = null;
        try{
            fr = new FileReader(path);
            br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null){
                String[] parts = sCurrentLine.split(" ");
                if (parts[0].startsWith("var")){
                    tree.put(parts[2], 1, parts[0] + " " + parts[1]);
                }else {
                    tree.put(parts[2], 1, parts[0] + " " + parts[1] + " " + parts[3]);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    private static void start() {
	// write your code here
        Scanner input;
        double s, e;
        long f1, f2;
        input = new Scanner(System.in);
        printMenu();
        while (true){
            int command = input.nextInt();
            switch (command){
                case 1:
                    System.out.println("Please insert relative or absolute file path:");
                    path = input.next();

                    s = Calendar.getInstance().getTime().getTime();
                    f1 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                    read_file_and_build_tree();

                    e = Calendar.getInstance().getTime().getTime();
                    f2 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                    System.out.println("    TIME: " + (e - s)/1000d + " s");
                    System.out.println("    MEMORY: " + Math.abs(f1 - f2)/ 1000000L + " Mb");
                    printMenu();
                    break;
                case 2:
                    System.out.println("Enter your prefix:");
                    String prefix = input.next();

                    s = Calendar.getInstance().getTime().getTime();
                    f1 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();
                    SuggestTree.Node node = tree.getAutocompleteSuggestions(prefix);

                    try{
                    for (int i=0; i<node.listLength(); i++){
                        SuggestTree.Entry entry = node.getSuggestion(i);
                        String parts = entry.getExtra_data();
                        if (entry.getTerm().equals(prefix))
                            tree.put(entry.getTerm(), entry.getWeight()+1, parts);
                        System.out.println(parts + " " + entry.getTerm());
                    }}catch (NullPointerException e1){
                        System.out.println("Not Found");
                        e = Calendar.getInstance().getTime().getTime();
                        f2 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                        System.out.println("    TIME: " + (e - s)/1000d);
                        System.out.println("    MEMORY: " + Math.abs(f1 - f2)/ 1000000L + " Mb");

                        printMenu();
                        break;
                    }

                    e = Calendar.getInstance().getTime().getTime();
                    f2 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();
                    System.out.println("    MEMORY: " + Math.abs(f1 - f2)/ 1000000L + " Mb");
                    System.out.println("    TIME: " + (e - s)/1000d);
                    printMenu();
                    break;
                case 3:
                    System.out.println("Enter in exact format to add in tree:");
                    input = new Scanner(System.in);
                    String add = input.nextLine();

                    s = Calendar.getInstance().getTime().getTime();
                    f1 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();;

                    String[] parts = add.split(" ");
                    if (parts[0].startsWith("var")){
                        tree.put(parts[2], 1, parts[0] + " " + parts[1]);
                    }else if (parts[0].startsWith("fun")){
                        tree.put(parts[2], 1, parts[0] + " " + parts[1] + " " + parts[3]);
                    }

                    System.out.println("Successfully added.");

                    e = Calendar.getInstance().getTime().getTime();
                    f2 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                    System.out.println("    TIME: " + (e - s)/1000d);
                    System.out.println("    MEMORY: " + Math.abs(f1 - f2)/ 1000000L + " Mb");

                    printMenu();
                    break;
                case 4:
                    System.out.println("Just Enter the exact name of variable or function:");

                    String name = input.next();

                    s = Calendar.getInstance().getTime().getTime();
                    f1 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                    tree.remove(name);

                    System.out.println("Deleted.");

                    e = Calendar.getInstance().getTime().getTime();
                    f2 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                    System.out.println("    TIME : " + (e - s)/1000d);
                    System.out.println("    MEMORY: " + Math.abs(f1 - f2)/ 1000000L + " Mb");

                    printMenu();
                    break;
                case 5:
                    s = Calendar.getInstance().getTime().getTime();
                    f1 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                    System.out.println("Size: " + tree.size());

                    e = Calendar.getInstance().getTime().getTime();
                    f2 = Runtime.getRuntime().maxMemory() + Runtime.getRuntime().freeMemory() - Runtime.getRuntime().totalMemory();

                    System.out.println("    TIME : " + (e - s)/1000d);
                    System.out.println("    MEMORY: " + Math.abs(f1 - f2)/ 1000000L + " Mb");

                    printMenu();
                    break;
                case 6:
                    return;
            }


        }

    }

    private static void printMenu(){
        System.out.println("=============================== MENU ===============================");
        System.out.println("Please type your choice number and press Enter:");
        System.out.println("1. Read file and get ready");
        System.out.println("2. Autocomplete feature");
        System.out.println("3. Add another function or variable");
        System.out.println("4. Remove a function or variable");
        System.out.println("5. Get the size of tree");
        System.out.println("6. Exit");
        System.out.println("====================================================================");
    }
}
