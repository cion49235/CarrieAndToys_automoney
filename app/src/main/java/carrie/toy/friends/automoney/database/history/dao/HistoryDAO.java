package carrie.toy.friends.automoney.database.history.dao;


import carrie.toy.friends.automoney.database.BasicDAO;

public interface HistoryDAO<T> extends BasicDAO<T> {
    T getLatestEntry();
}
