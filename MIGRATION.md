(sooner or later I will translate this to english, please create an issue if this could be useful to you!)

# Database migration from U.O.me to Debitum
Achtung: nur "Simple Debts" werden migriert, da Debitum (noch) keine Unterstützung für Gruppen hat!

1. Datenbank aus U.O.me (backup!) und (leere) Datenbank aus Debitum exportieren
2. Datenbank aus Debitum in "DB Browser for sqlite" öffnen
3. "Datenbank anhängen" -> uome.backup auswählen, Name "uome"
    (Bei Verwendung der SQLite-Kommandozeile wäre der entsprechende Befehl `ATTACH DATABASE 'uome.backup' AS uome;`
4. Debitum-Tabellen leeren: `delete from person;` und `delete from txn;`
5. Personen aus U.O.me nach Debitum migrieren:
    ```sql
    insert into person 
    select _id, name 
    from uome.person_table 
    where idGroup=0;
    ```
    Ab v1.1.0 (Notiz für Person) sollte folgendes genutzt werden (ungetestet):
    ```sql
    insert into person 
    select _id, name, description 
    from uome.person_table 
    where idGroup=0;
    ```
6. Transaktionen aus U.O.me nach Debitum migrieren
    ```sql
    insert into txn 
    select _id, 
    case 
    	 when financial=0 then 
    		case 
    			when direction='WITHDRAWAL' then -1 
    			else 1
    		end
    	else 
    		case
    			when direction='WITHDRAWAL' then -CAST(value*100 as INT)
    			else CAST(value*100 as INT)
    		end
    end as "amount", 
    personId, 
    case when financial=0 then value else description end as "description", 
    financial, 
    "dateTime"
    from uome.transaction_table where groupId = 0;
    ```
7. Änderungen speichern
8. Datenbank `debitum_backup.db` nach **[external storage]/Android/data/org.ebur.debitum/files/backup/** kopieren/verschieben und aus Debitum heraus die Wiederherstellung starten


tested with U.O.me 3.1.0 and Debitum v1.0.0
