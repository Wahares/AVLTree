import java.io.*;
import java.util.Scanner;

public class ASD3 {

    public static void main(String[] args) throws FileNotFoundException {

        File file = new File(args[0]);
        Scanner scan = new Scanner(file);

        //odczyt ilości operacji
        int k = Integer.parseInt(scan.nextLine());

        //tworzenie drzewa AVL
        AVLTree tree = new AVLTree();
        while (scan.hasNextInt())
            tree.append(scan.nextInt());

        int p = 1;

        //wykonanie k operacji
        for (int i = 0; i < k; i++) {
            TreeNode selected = tree.selectNth(p);
            int X;
            int next = p % tree.root.size + 1; //indeks elementu następnego
            if (selected.value % 2 == 0) {
                //DELETE
                if (tree.root.size == 1) { //jeśli usuwamy jedyny węzeł w drzewie to usuwamy całe drzewo
                    tree.root = null;
                    break;
                }
                if (p == tree.root.size)//usunięty element był na lewo od
                    p--;                //wskaźnika -> wskaźnik przesunął się w lewo

                X = tree.selectNth(next).value;
                tree.deleteNth(next);
            } else {
                //ADD
                X = selected.value;

                if (next == 1)          //ciąg ma charakter cykliczny, więc wstawienie elementu
                    next += tree.root.size;//na początek ciągu możemy uznać za wstawienie go na koniec

                tree.insertAtNth(X - 1, next);
            }
            p = (p + X - 1) % tree.root.size + 1;
        }

        //wyświetlanie ciągu jeśli drzewo dalej istnieje
        if (tree.root != null)
            for (int i = 0; i < tree.root.size; i++) {
                System.out.print(tree.selectNth((i + p - 1) % tree.root.size + 1).value);
                if (i < tree.root.size - 1)
                    System.out.print(" ");
            }
    }

}

class AVLTree {
    public TreeNode root;

    public void append(int value) {//dodawanie wartości na ostatnie miejsce w drzewie = ostatnie w ciągu
        if (root == null)
            root = new TreeNode(this, value, null);
        else
            insertAtNth(value, root.size + 1);
    }

    public void insertAtNth(int value, int n) {
        TreeNode tmp = selectNth(n - 1);
        TreeNode newNode;

        if (tmp.R == null) {
            newNode = new TreeNode(this, value, tmp);
            tmp.R = newNode;
        } else {
            tmp = tmp.R.minNode();

            newNode = new TreeNode(this, value, tmp);
            tmp.L = newNode;
        }
        newNode.fixHeights();
        newNode.fixSizes();
        newNode.checkWeights();

    }

    public void deleteNth(int n) {

        TreeNode nodeToDelete = selectNth(n);

        //najniższy węzeł w "ciągu" węzłów, w którym zaszły zmiany po usunięciu n-tego elementu
        TreeNode nodeToFixFrom = null;

        switch (nodeToDelete.noChild()) {
            case 0 -> { //zero dzieci
                nodeToDelete.parent.replaceChild(nodeToDelete, null);
                nodeToFixFrom = nodeToDelete.parent;
            }
            case 1 -> { //jedno dziecko
                nodeToDelete.parent.replaceChild(nodeToDelete
                        , nodeToDelete.L != null ? nodeToDelete.L : nodeToDelete.R);
                (nodeToDelete.L != null ? nodeToDelete.L : nodeToDelete.R).parent = nodeToDelete.parent;
                nodeToFixFrom = nodeToDelete.parent;
            }
            case 2 -> { //dwoje dzieci
                TreeNode toReplace = nodeToDelete.L.maxNode();
                nodeToFixFrom = toReplace.parent;
                nodeToDelete.value = toReplace.value; //skopiowanie węzła na nowe miejsce
                deleteNth(n - 1);   //usunięcie starego węzła
            }
        }
        if (nodeToFixFrom == null)
            return;
        //naprawianie właściwości w węzłów idąc od dołu do góry
        nodeToFixFrom.fixHeights();
        nodeToFixFrom.fixSizes();
        nodeToFixFrom.checkWeights();
    }

    public TreeNode selectNth(int n) {
        return selectNthRecursive(root, n);
    }

    private TreeNode selectNthRecursive(TreeNode source, int n) {
        if (source == null)
            return null;
        int tmp = n - 1;
        if (source.L != null)
            tmp -= source.L.size;
        if (tmp == 0)
            return source;
        if (tmp < 0)
            return selectNthRecursive(source.L, n); //szukany indeks jest na lewo
        else
            return selectNthRecursive(source.R, tmp); //szukany indeks jest na prawo
    }
}

class TreeNode {

    private final AVLTree mainTree;
    public int value;
    public TreeNode parent, L, R;

    // height - wysokość poddrzewa
    // size - ilość węzłów w poddrzewie wraz z korzeniem
    public int height, size;

    public TreeNode(AVLTree mainTree, int value, TreeNode parent) {
        this.mainTree = mainTree;
        this.value = value;
        this.parent = parent;
        this.height = 0;
        this.size = 1;
    }

    public int getWeight() {//waga do balansowania drzewa
        int weight = 0;
        if (L != null)
            weight = L.height + 1;
        if (R != null)
            weight -= R.height + 1;
        return weight;
    }

    public TreeNode minNode() { //dziecko pierwsze względem kolejności odczytu poniżej tego węzła
        if (L != null)
            return L.minNode();
        return this;
    }

    public TreeNode maxNode() { //dziecko ostatnie względem kolejności odczytu poniżej tego węzła
        if (R != null)
            return R.maxNode();
        return this;
    }


    public void fixHeights() { //naprawianie wysokości drzew po zmianach idąc od dołu do góry
        height = 0;
        if (R != null)
            height = R.height + 1;
        if (L != null)
            if (L.height + 1 > height)
                height = L.height + 1;
        if (parent != null)
            parent.fixHeights();
    }

    public void fixSizes() { //naprawianie wielkości drzew po zmianach idąc od dołu do góry
        size = 1;
        if (R != null)
            size += R.size;
        if (L != null)
            size += L.size;
        if (parent != null)
            parent.fixSizes();
    }

    public void checkWeights() { //sprawdzanie wagi po zmianach i aplikuje rotacje jeśli potrzebne

        if (getWeight() == 2) {
            if (L.getWeight() < 0) { //LR
                L.rotate(true);
                rotate(false);
            } else {                //LL
                rotate(false);
            }

        } else if (getWeight() == -2) {

            if (R.getWeight() > 0) { //RL
                R.rotate(false);
                rotate(true);
            } else {                //RR
                rotate(true);
            }

        }
        if (parent != null)
            parent.checkWeights();
    }

    public int noChild() { //ilosc dzieci
        int tmp = 0;
        if (L != null)
            tmp++;
        if (R != null)
            tmp++;
        return tmp;
    }

    public void replaceChild(TreeNode from, TreeNode to) {//zamiana referencji dziecka
        if (L == from)
            L = to;
        if (R == from)
            R = to;
    }

    public void rotate(boolean left) {

        TreeNode pivot, root;

        pivot = left ? R : L;
        root = this;

        if (left) {
            root.R = pivot.L;
            if (pivot.L != null)
                pivot.L.parent = root;
            pivot.L = root;
        } else {
            root.L = pivot.R;
            if (pivot.R != null)
                pivot.R.parent = root;
            pivot.R = root;
        }
        pivot.parent = root.parent;
        root.parent = pivot;
        if (pivot.parent == null)
            mainTree.root = pivot;
        else
            pivot.parent.replaceChild(root, pivot);

        //naprawa wysokości i wielkości po zmianach
        root.fixHeights();
        root.fixSizes();
    }
}
