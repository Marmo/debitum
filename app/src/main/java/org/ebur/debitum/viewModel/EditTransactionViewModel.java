package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.database.ImageRepository;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.TransactionWithPerson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditTransactionViewModel extends AndroidViewModel {

    private final PersonRepository personRepository;
    private final TransactionRepository transactionRepository;
    private final ImageRepository imageRepository;
    private TransactionWithPerson transaction;
    // maintained separately from transaction for new transactions (transaction = null)
    private int transactionType;
    private Date timestamp;
    private Date returnTimestamp;
    private final MutableLiveData<List<String>> imageFilenames;


    public EditTransactionViewModel(Application application) {
        super(application);
        personRepository = new PersonRepository(application);
        transactionRepository = new TransactionRepository(application);
        imageRepository = new ImageRepository(application);
        imageFilenames = new MutableLiveData<>();
    }

    public List<Person> getPersons() throws ExecutionException, InterruptedException { return  personRepository.getAllPersonsNonLive(); }

    public void setTransaction(TransactionWithPerson twp) {
        this.transaction = twp;
        if (twp != null) {
            this.transactionType = twp.transaction.isMonetary ? Transaction.TYPE_MONEY : Transaction.TYPE_ITEM;
        }
    }
    public TransactionWithPerson getTransaction() {
        return transaction;
    }

    public void setTransactionType(int type) {
        this. transactionType = type;
    }
    public boolean isMoneyTransaction() {
        return transactionType == Transaction.TYPE_MONEY;
    }
    public boolean isItemTransaction() {
        return transactionType == Transaction.TYPE_ITEM;
    }

    public int getPersonId(String name) throws ExecutionException, InterruptedException { return personRepository.getPersonId(name); }

    public boolean isNewTransaction() { return transaction == null; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Date getTimestamp() { return this.timestamp; }
    public void setReturnTimestamp(Date timestamp) { this.returnTimestamp = timestamp; }
    public Date getReturnTimestamp() { return this.returnTimestamp; }

    public TransactionWithPerson getTransactionFromDatabase(int idTransaction) throws ExecutionException, InterruptedException { return transactionRepository.getTransaction(idTransaction); }
    public Long insert(Transaction transaction) { return transactionRepository.insert(transaction); }
    public void update(Transaction transaction) { transactionRepository.update(transaction); }
    public void delete(Transaction transaction) { transactionRepository.delete(transaction); }

    public LiveData<List<String>> getImageFilenames() {return imageFilenames;}
    public void loadImageFilenamesFromDb() {
        if (transaction != null) {
            imageFilenames.setValue(imageRepository.getImageFilenames(transaction.transaction.idTransaction));
        } else {
            imageFilenames.setValue(new ArrayList<>());
        }
    }
    public boolean hasImages() {
        return imageFilenames.getValue() != null && !imageFilenames.getValue().isEmpty();
    }
    public void addImageLink(@NonNull String filename) {
        List<String> images = imageFilenames.getValue();
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(filename);
        imageFilenames.setValue(images);
    }

    public void deleteImageLink(@NonNull String filename) {
        List<String> images = imageFilenames.getValue();
        if (images != null) {
            images.remove(filename);
            imageFilenames.setValue(images);
        }
    }

    public void deleteOrphanedImageFiles(File imageBasedir) {
        List<String> filenamesDb = imageRepository.getAllImageFilenames();
        String[] filenamesDir = imageBasedir.list();
        assert filenamesDir != null;
        for (String filenameDir:filenamesDir) {
            if (!filenamesDb.contains(filenameDir)) {
                File orphanedImageFile = new File(imageBasedir, filenameDir);
                orphanedImageFile.delete();
            }
        }
    }

    public void deleteBrokenImageLinks(File imageBasedir) {
        File[] files = imageBasedir.listFiles();
        // files would be null, if this imageBasedir does not denote a directory, or if an I/O error
        // occurs. In this case deleting image links is skipped. Thus some possibly broken image
        // links stay in the database, which is a purely internal nuisance.
        if (files != null) {
            imageRepository.deleteBrokenImageLinks(Arrays.asList(files));
        }
    }

    public void synchronizeDbWithViewModel(File imageBasedir) {
        synchronizeDbWithViewModel(imageBasedir, transaction.transaction.idTransaction);
    }
    public void synchronizeDbWithViewModel(File imageBasedir, int idTransaction) {
        // update the transaction's image links
        imageRepository.update(idTransaction, imageFilenames.getValue());
        // then remove all images from the filesystem, that are not linked to any transaction
        deleteOrphanedImageFiles(imageBasedir);
        // remove all image links from the db that have no matching image file
        deleteBrokenImageLinks(imageBasedir);
    }
}