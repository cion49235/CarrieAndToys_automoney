package carrie.toy.friends.automoney.fragments.list;


import carrie.toy.friends.automoney.fragments.ViewContract;

public interface ListViewContract<I, N> extends ViewContract<I> {
    void showListFooter(boolean show);

    void handleNextItems(N result);
}
