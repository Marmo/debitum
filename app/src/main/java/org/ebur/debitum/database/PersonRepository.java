package org.ebur.debitum.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PersonRepository {

    private final PersonDao personDao;

    private final LiveData<List<Person>> allPersons;

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

    public List<Person> getAllPersonsNonLive() throws ExecutionException, InterruptedException {
        Future<List<Person>> future = AppDatabase.databaseTaskExecutor.submit(personDao::getAllPersonsNonLive);
        return future.get();
    }

    public void insert(Person person) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            personDao.insert(person);
        });
    }

    public void update(Person person) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            personDao.update(person);
        });
    }

    public void delete(Person person) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            personDao.delete(person);
        });
    }

    public int getPersonId(String name) throws ExecutionException, InterruptedException {
        Future<Integer> future = AppDatabase.databaseTaskExecutor.submit( () -> personDao.getPersonId(name));
        return future.get();
    }

    public Person getPersonById(int id)throws ExecutionException, InterruptedException {
        Future<Person> future = AppDatabase.databaseTaskExecutor.submit( () -> personDao.getPersonById(id));
        return future.get();
    }

    public boolean exists(String name) throws ExecutionException, InterruptedException {
        Future<Boolean> future = AppDatabase.databaseTaskExecutor.submit( () -> personDao.exists(name));
        return future.get();
    }
}
