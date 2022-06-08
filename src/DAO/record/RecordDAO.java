package DAO.record;

import java.util.List;

public interface RecordDAO {

    void addRecord(Record record);

    void deleteRecord(int row);

    List<String[]> getAllRecords();
}
