package org.ebur.debitum.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PersonRepository {

    private PersonDao personDao;

    private LiveData<List<Person>> allPersons;

    // Note that in order to unit test the Repository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public PersonRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        personDao = db.personDao();
        allPersons = personDao.getAllPersons();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<Person>> getAllPersons() { return allPersons; }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Person person) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            personDao.insert(person);
        });
    }

    public boolean exists(String name) {
        // TODO use query in DAO, but must not be run on main thread. How?
        // return personDao.exists(name);
        for (Person person : allPersons.getValue()) {
            if(person.name.equals(name)) return true;
        }
        return false;
    }
}
