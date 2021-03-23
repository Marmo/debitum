package org.ebur.debitum.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.ebur.debitum.model.Person;

import java.util.List;

@Dao
public interface PersonDAO {

    @Query("SELECT * FROM person")
    List<Person> getAll();

    @Insert
    void insertAll(Person... persons);

    @Delete
    void delete(Person person);
}
