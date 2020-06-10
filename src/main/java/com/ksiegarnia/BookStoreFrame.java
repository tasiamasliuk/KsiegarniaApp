package com.ksiegarnia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
//import javax.swing.CheckBoxList;

class BookStoreFrame extends JFrame {
    // url for connection to DB
    private String jdbcUrl = "jdbc:mysql://localhost:3306/ksiegarnia", jdbcUser = "root", jdbcPass = "";

    // log information field
    private JTextField log = new JTextField();

    // ---------------- PANELS -------------------------
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JPanel panelClient = new JPanel(); // klienci
    private JPanel panelBooks = new JPanel(); // ksiązki
    private JPanel panelOrder = new JPanel(); // zamówiemia

    // ------------- on panelCLIENT ----------------------
    private JTextField polePesel = new JTextField();
    private JTextField poleImia = new JTextField();
    private JTextField poleNazwisko = new JTextField();
    private JTextField poleUrodziny = new JTextField();
    private JTextField poleMail = new JTextField();
    private JTextField poleAdres = new JTextField();
    private JTextField polePhone = new JTextField();

    private DefaultListModel<String> listModelClient = new DefaultListModel<>();
    private JList<String> listClient = new JList<>(listModelClient);
    private JScrollPane scrollPaneClient = new JScrollPane(listClient);

    private JButton buttonSaveClient = new JButton("Zapisz");
    private JButton buttonDellClient = new JButton("Usuń");

    //-------------- on panelBook ----------------------
    private JTextField poleISBN = new JTextField();
    private JTextField poleAutorBook = new JTextField();
    private JTextField poleTytulBook = new JTextField();
    private JComboBox comboTypBook = new JComboBox();
    private JTextField poleWydawBook = new JTextField();
    private JTextField poleRokBook = new JTextField();
    private JTextField poleCenaBook = new JTextField();

    private DefaultListModel<String> listModelBooks = new DefaultListModel<>();
    private JList<String> listBooks = new JList<>(listModelBooks);
    private JScrollPane scrollPaneBooks = new JScrollPane(listBooks);

    private JButton buttonSaveBook = new JButton("Zapisz");
    private JButton buttonDellBook = new JButton("Usuń");

    //-------------- change price book --------------
    private JTextField poleNewPrice = new JTextField();
    private JButton buttonNewPrice = new JButton("Zmień cenę");

    //-------------- on panelORDER -------------------
    private JTextField poleDataOrder = new JTextField();
    private JComboBox comboStatusOrder = new JComboBox();

    private DefaultListModel<String> listModelKlientOrder = new DefaultListModel<>();
    private JList<String> listKlientOrder = new JList<>(listModelKlientOrder);
    private JScrollPane scrollPaneKlientOrder = new JScrollPane(listKlientOrder);

    private DefaultListModel<String> listModelBookOrder = new DefaultListModel<>();
    private JList<String> listBookOrder = new JList<>(listModelBookOrder);
    private JScrollPane scrollPaneBookOrder = new JScrollPane(listBookOrder);

    private JButton buttonNewOrder = new JButton("Dodać zamówienia");

    private DefaultListModel<String> listModelOrder = new DefaultListModel<>();
    private JList<String> listOrder = new JList<>(listModelOrder);
    private JScrollPane scrollPaneOrder = new JScrollPane(listOrder);

    private JComboBox comboNewStatusOrder = new JComboBox();
    private JButton buttonNewStatusOrder = new JButton("Zmień status");


    //function to update date on list CLIENT
    private void updateClientList() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT klienci.pesel, nazwisko, imie, adres FROM klienci, kontakty WHERE klienci.pesel = kontakty.pesel ORDER BY nazwisko, imie";
            ResultSet res = stmt.executeQuery(sql);
            listModelClient.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3) + ", " + res.getString(4);
                listModelClient.addElement(s);
            }
        }
        catch (SQLException ex) {
            log.setText("nie udało się zaktualizować listy klientów");
        }
    }

    /* validation for date
    * Date format for SQL INSERT in DB
    * yyy-mm-dd
     */
    private boolean isDataValid(String dataInput){
        int y = Integer.parseInt(dataInput.substring(0,4));
        int m = Integer.parseInt(dataInput.substring(5,7));
        int d = Integer.parseInt(dataInput.substring(8,10));

        /*System.out.println("Input DATE:\ny - " + y +
        *                               "\nm - " + m +
        *                               "\nd - " + d);
        */

        if (! dataInput.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}"))
            return false;
        if (y < 1910 || 2004 < y )
            return false;
        if (m < 1 || 12 < m)
            return false;
        if (d < 1 || 31 < d)
            return false;
        return true;
    }

    // ActionListener for buttonSaveClient
    private ActionListener akc_zap_kli = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String pesel = polePesel.getText();
            if (! pesel.matches("[0-9]{3,11}")) {
                JOptionPane.showMessageDialog(BookStoreFrame.this, "błąd w polu z peselm");
                polePesel.setText("");
                polePesel.requestFocus();
                return;
            }

            String imie = poleImia.getText();
            String nazwisko = poleNazwisko.getText();
            String ur = poleUrodziny.getText();
            if (! isDataValid(ur)) {
                JOptionPane.showMessageDialog(BookStoreFrame.this, "Data [rok-miesiec-dzien] np.1987-04-30");
                poleUrodziny.setText("");
                poleUrodziny.requestFocus();
                return;
            }
            if (imie.equals("") || nazwisko.equals("") || ur.equals("")) {
                JOptionPane.showMessageDialog(BookStoreFrame.this, "nie wypełnione pole z imieniem lub nazwiskiem lub datą urodzenia");
                return;
            }

            String mail = poleMail.getText();
            /*     //--- validation for mail ---
            if(! mail.matches("[A-Z0-9._%+-]@[A-Z0-9.-].[A-Z]{2,6}")){
                JOptionPane.showMessageDialog(BookStoreFrame.this,"Prosze podac korektny mail adres");
                poleMail.setText("");
                poleMail.requestFocus();
                return;
            }*/
            String adr = poleAdres.getText();
            String tel = polePhone.getText();
            if (mail.equals("") || adr.equals("") || tel.equals("")) {
                JOptionPane.showMessageDialog(BookStoreFrame.this, "nie wypełnione pole z emailem lub adresem");
                return;
            }

            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sqlReqestKlient = "INSERT INTO klienci (pesel, imie, nazwisko, ur) VALUES('" + polePesel.getText() + "', '" + poleImia.getText() + "', '" + poleNazwisko.getText() + "', '" + poleUrodziny.getText() + "')";
                int res = stmt.executeUpdate(sqlReqestKlient);
                if (res == 1) {
                    log.setText("OK - klient dodany do bazy");
                    String sqlRequestKontakt = "INSERT INTO kontakty (pesel, mail, adres, tel) VALUES('" + polePesel.getText() + "', '" + poleMail.getText() + "', '" + poleAdres.getText() + "', '" + polePhone.getText() + "')";
                    stmt.executeUpdate(sqlRequestKontakt);
                    updateClientList();
                }
            }
            catch(SQLException ex) {
                log.setText("błąd SQL - nie zapisano klienta: " + ex);
            }
        }
    };

    // ActionListener for buttonDellClient
    private ActionListener akc_usun_kli = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            log.setText(listClient.getModel().getElementAt(listClient.getSelectionModel().getMinSelectionIndex()));
            if (listClient.getSelectedIndices().length == 0) return; // listClient.getSelectionModel().getSelectedItemsCount() == 0
            String p = listClient.getModel().getElementAt(listClient.getSelectionModel().getMinSelectionIndex());
            System.out.println("\nSelected item from JList:  " + p);
            p = p.substring(0, p.indexOf(':'));
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) FROM zamowienia WHERE pesel = '" + p + "'";
                ResultSet res = stmt.executeQuery(sql);
                res.next();
                int k = res.getInt(1);
                if (k == 0) {
                    String sql1 = "DELETE FROM klienci WHERE pesel = '" + p + "'";
                    stmt.executeUpdate(sql1);
                    String sql2 = "DELETE FROM kontakty WHERE pesel = '" + p + "'";
                    stmt.executeUpdate(sql2);
                    log.setText("OK - klient usunięty bazy");
                    updateClientList();
                }
                else log.setText("nie usunięto klienta, ponieważ składał już zamówienia");
            }
            catch (SQLException ex) {
                log.setText("błąd SQL - nie ununięto klienta");
            }
        }
    };

    //function to update date on list BOOKS
    private void updateBooksList() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT `isbn`, `autor`, `tytul`, `typ`, `rok`, `cena` FROM `ksiazki` WHERE 1 ORDER BY autor, tytul";
            ResultSet res = stmt.executeQuery(sql);
            listModelBooks.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + ": " + res.getString(3) + ": " + res.getString(4) + " " + res.getString(5) + " " + res.getString(6);
                listModelBooks.addElement(s);
            }
        }
        catch (SQLException ex) {
            log.setText("nie udało się zaktualizować listy klientów");
        }
    }

    // ActionListener for buttonSaveBook
    private ActionListener akc_zap_ksia = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String ISBN = poleISBN.getText();
            if (! ISBN.matches("[0-9]{3,11}")) {
                JOptionPane.showMessageDialog(BookStoreFrame.this, "błąd w polu z ISBN");
                poleISBN.setText("");
                poleISBN.requestFocus();
                return;
            }

            String autor = poleAutorBook.getText();
            String tytul = poleTytulBook.getText();

            TypBook typBook;
            typBook = (TypBook)comboTypBook.getItemAt(comboTypBook.getSelectedIndex());
            String typ = typBook.name();
            if (autor.equals("") || tytul.equals("") || typ.equals("")) {
                JOptionPane.showMessageDialog(BookStoreFrame.this, "nie wypełnione pole z awtorem, tytulem lub typem książki");
                return;
            }

            String wydaw = poleWydawBook.getText();
            String rok = poleRokBook.getText();
            String cena = poleCenaBook.getText();
            if (cena.equals("")) {
                JOptionPane.showMessageDialog(BookStoreFrame.this, "nie wypełnione pole z ceną");
                return;
            }

            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sqlInsertKsia = "INSERT INTO `ksiazki`(`isbn`, `autor`, `tytul`, `typ`, `wydawnictwo`, `rok`, `cena`) VALUES ('" + ISBN + "','" + autor + "','" + tytul + "','" + typ + "','" + wydaw + "','" + rok + "','" + cena + "')";
                int res = stmt.executeUpdate(sqlInsertKsia);
                if (res == 1) {
                    log.setText("OK - książka dodana do bazy");
                    updateBooksList();
                }
            }
            catch(SQLException ex) {
                log.setText("błąd SQL - nie zapisano klienta: " + ex);
            }
        }
    };

    // ActionListener for buttonDellBook
    private ActionListener akc_usun_ksia = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            log.setText(listBooks.getModel().getElementAt(listBooks.getSelectionModel().getMinSelectionIndex()));
            if (listBooks.getSelectedIndices().length == 0) // listClient.getSelectionModel().getSelectedItemsCount() == 0
                return;
            String p = listBooks.getModel().getElementAt(listBooks.getSelectionModel().getMinSelectionIndex());
            System.out.println("\nSelected item from JList for delete:  " + p);
            p = p.substring(0, p.indexOf(':'));
            try (Connection conn=DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) FROM zestawienia WHERE isbn = '" + p + "'";
                ResultSet res = stmt.executeQuery(sql);
                res.next();
                int k = res.getInt(1);
                if (k == 0) {
                    String sql1 = "DELETE FROM ksiazki WHERE isbn = '" + p + "'";
                    stmt.executeUpdate(sql1);
                    log.setText("OK - ksiazke usunięta z bazy");
                    updateBooksList();
                }
                else log.setText("nie usunięto ksiazkie, ponieważ zamówuwiona");
            }
            catch (SQLException ex) {
                log.setText("błąd SQL - nie ununięto ksiazke");
            }
        }
    };

    // ------- function to change price ---
    private ActionListener chenge_price_book = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String newPrice = poleNewPrice.getText();

            log.setText(listBooks.getModel().getElementAt(listBooks.getSelectionModel().getMinSelectionIndex()));
            if (listBooks.getSelectedIndices().length == 0 || newPrice.equals("")) // listClient.getSelectionModel().getSelectedItemsCount() == 0
                return;
            String p = listBooks.getModel().getElementAt(listBooks.getSelectionModel().getMinSelectionIndex());
            System.out.println("\nSelected item from JList for change prise:  " + p);
            p = p.substring(0, p.indexOf(':'));
            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sql = "UPDATE `ksiazki` SET `cena`='" + newPrice + "' WHERE `isbn`=`" + p + "`";

                int res = stmt.executeUpdate(sql);
                if (res == 1) {
                    log.setText("OK - cena książki zmieniona");
                    updateBooksList();
                } else {
                    log.setText("nie zmieniono cenę ksiazki");
                }
            } catch (SQLException ex) {
                log.setText("błąd SQL - nie ununięto ksiazke");
            }
        }
    };


    //function to update date on list CLIENT on ORDER panel
    private void updateClientListOrder() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT klienci.pesel, nazwisko, imie FROM klienci ORDER BY nazwisko, imie";
            ResultSet res = stmt.executeQuery(sql);
            listModelKlientOrder.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3);
                listModelKlientOrder.addElement(s);
            }
        }
        catch (SQLException ex) {
            log.setText("nie udało się zaktualizować listy klientów");
        }
    }

    //function to update date on list BOOK on ORDER panel
    private void updateBooksListOrder() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sqlSelect = "SELECT isbn, autor, tytul, cena FROM ksiazki ORDER BY tytul";
            ResultSet res = stmt.executeQuery(sqlSelect);
            listModelBookOrder.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3);
                listModelBookOrder.addElement(s);
            }
        }
        catch (SQLException ex) {
            log.setText("nie udało się zaktualizować listy klientów");
        }
    }

    /* validation for date
     * Date format for SQL INSERT in DB
     * yyy-mm-dd
     * year mast be 2020 or greater
     * TODO add checking by today date (not greater then today)
     */
    private boolean isDataOrderValid(String dataInput){
        int y = Integer.parseInt(dataInput.substring(0,4));
        int m = Integer.parseInt(dataInput.substring(5,7));
        int d = Integer.parseInt(dataInput.substring(8,10));

        /*System.out.println("Input DATE:\ny - " + y +
         *                               "\nm - " + m +
         *                               "\nd - " + d);
        */

        if (! dataInput.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}"))
            return false;
        if (2020 > y)
            return false;
        if (m < 1 || 12 < m)
            return false;
        if (d < 1 || 31 < d)
            return false;
        return true;
    }

    /* ------- function to add new order ---
    *  Tables to update on DB:
    *  `zamowienia`
    *  `zestawienia`
    */
    private ActionListener akc_add_order = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String orderDate = poleDataOrder.getText();
            if (!isDataOrderValid(orderDate)){
                JOptionPane.showMessageDialog(BookStoreFrame.this, "Data [rok-miesiec-dzien] np.2020-05-17");
                poleDataOrder.setText("");
                poleDataOrder.requestFocus();
                return;
            }

            Status status;
            status = (Status) comboStatusOrder.getItemAt(comboStatusOrder.getSelectedIndex());
            String orderStatus = status.name();

            if (listBookOrder.getSelectedIndices().length == 0 || listKlientOrder.getSelectedIndices().length == 0)
                return;

            String p = listKlientOrder.getModel().getElementAt(listKlientOrder.getSelectionModel().getMinSelectionIndex());
            System.out.println("\nSelected Client from JList for create an order:  " + p);
            String orderClientKey = p.substring(0, p.indexOf(':'));
            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();

                String sqlInsertOrder = "INSERT INTO `zamowienia`(`pesel`, `kiedy`, `status`) VALUES ('" + orderClientKey + "', '" + orderDate + "', '" + orderStatus + "')";
                int resInsertOrder = stmt.executeUpdate(sqlInsertOrder);

                String sqlSelectOrderKey = "SELECT `id` FROM `zamowienia` WHERE `pesel`= " + orderClientKey + " AND `kiedy` = '" + orderDate + "' AND `status` = '" + orderStatus + "'";
                ResultSet resSelectOrderKey = stmt.executeQuery(sqlSelectOrderKey);
                resSelectOrderKey.next();
                int orderKey = resSelectOrderKey.getInt(1);
                System.out.println("\n Added order with PK: " + orderKey);

                System.out.println("\n Selected " + listBookOrder.getSelectedIndices().length + " books for order\n");
                for (int i = 0; i < listBookOrder.getSelectedIndices().length; i++) {
                    String bookToOrder = listBookOrder.getModel().getElementAt(i);
                    System.out.println(i+1 + " selected book from JList for order:  " + bookToOrder);

                    String keyBookToOrder = bookToOrder.substring(0, bookToOrder.indexOf(':'));
                    System.out.println("Key ordered book: " + keyBookToOrder);
                    String sqlSelectOrderBookPrice = "SELECT `cena` FROM `ksiazki` WHERE `isbn`=" + keyBookToOrder;
                    ResultSet resSelectOrderBookPrice = stmt.executeQuery(sqlSelectOrderBookPrice);
                    resSelectOrderBookPrice.next();
                    double orderBookPrice = resSelectOrderBookPrice.getDouble(1);
                    System.out.println("  witch price: " + orderBookPrice);

                    System.out.println("INSERT INTO `zestawienia`(`id`, `isbn`, `cena`) VALUES (`" + orderKey + "`, `" + keyBookToOrder + "` ,`" + orderBookPrice + "`)");
                    String sqlInsertOrderBook = "INSERT INTO `zestawienia`(`id`, `isbn`, `cena`) VALUES (" + orderKey + ", " + keyBookToOrder + " ," + orderBookPrice + ")";
                    int resInsertOrderBook = stmt.executeUpdate(sqlInsertOrderBook);
                    System.out.println(i + " book added result: " + resInsertOrderBook);
                }

                if (resInsertOrder == 1) {
                    log.setText("OK - cena książki zmieniona");
                    updateOrderList();
                } else {
                    log.setText("nie zmieniono cenę ksiazki");
                }
            } catch (SQLException ex) {
                log.setText("błąd SQL - nie ununięto ksiazke");
            }
        }
    };

    //function to update date on list ORDER on ORDER panel
    private void updateOrderList() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sqlSelectFromZamow = "SELECT book.id, kiedy, pesel, status, COUNT(*) FROM `zestawienia` book INNER JOIN zamowienia ord ON ord.id = book.id GROUP BY book.id";
            ResultSet resOrder = stmt.executeQuery(sqlSelectFromZamow);
            listModelOrder.clear();

            while(resOrder.next()) {
                String orderListItem = resOrder.getString(1) + ": " + resOrder.getString(2) + " " + resOrder.getString(3) + " " + resOrder.getString(4) + " zamówiono " + resOrder.getString(5) + " książek";
                listModelOrder.addElement(orderListItem);
            }
        }
        catch (SQLException ex) {
            log.setText("nie udało się zaktualizować listy zamówień " + ex);
            System.out.println(ex);
        }
    }

    // ------- function to change status ---
    private ActionListener akc_change_status = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Status status;
            status = (Status)comboNewStatusOrder.getItemAt(comboNewStatusOrder.getSelectedIndex());
            String orderStatus = status.name();

            log.setText(listOrder.getModel().getElementAt(listOrder.getSelectionModel().getMinSelectionIndex()));
            if (listOrder.getSelectedIndices().length == 0)
                return;
            String selectedOrder = listOrder.getModel().getElementAt(listOrder.getSelectionModel().getMinSelectionIndex());
            System.out.println("\nSelected item from JList to change status:  " + selectedOrder);
            String keyOrder = selectedOrder.substring(0, selectedOrder.indexOf(':'));

            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                System.out.println("UPDATE `zamowienia` SET `status`='" + orderStatus + "' WHERE `id`='" + keyOrder + "'");
                String sql = "UPDATE `zamowienia` SET `status`='" + orderStatus + "' WHERE `id`='" + keyOrder + "'";
                int res = stmt.executeUpdate(sql);
                if (res == 1) {
                    log.setText("OK - status zamówienia zmieniony");
                    updateOrderList();
                } else {
                    log.setText("nie zmieniono statusu zamuwienia");
                }
            } catch (SQLException ex) {
                log.setText("błąd SQL - nie zmieniono statusu zamuwienia");
            }
        }
    };

    public BookStoreFrame() throws SQLException {
        super("Księgarnia wysyłkowa");
        setSize(660, 460);
        setLocation(100, 100);
        setResizable(false);

        // add panels
        tabbedPane.addTab("klienci", panelClient);
        tabbedPane.addTab("książki", panelBooks);
        tabbedPane.addTab("zamówienia", panelOrder);
        // add by border layout container
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        // information on bottom uneditable
        log.setEditable(false);
        getContentPane().add(log, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);



        // ----------- PANEL CLIENT --------
        panelClient.setLayout(null);

        // ----------- PESEL ---------------
        JLabel lab1 = new JLabel("pesel:");
        panelClient.add(lab1);
        lab1.setSize(100, 20);
        lab1.setLocation(40, 20);
        lab1.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(polePesel);
        polePesel.setSize(200, 20);
        polePesel.setLocation(160, 20);

        // ----------- IMIE -----------------
        JLabel lab2 = new JLabel("imię:");
        panelClient.add(lab2);
        lab2.setSize(100, 20);
        lab2.setLocation(40, 60);
        lab2.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(poleImia);
        poleImia.setSize(200, 20);
        poleImia.setLocation(160, 60);

        // ----------- NAZWISKO ---------------
        JLabel lab3 = new JLabel("nazwisko:");
        panelClient.add(lab3);
        lab3.setSize(100, 20);
        lab3.setLocation(40, 100);
        lab3.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(poleNazwisko);
        poleNazwisko.setSize(200, 20);
        poleNazwisko.setLocation(160, 100);

        // ----------- DATE ---------------------
        JLabel lab4 = new JLabel("data urodzenia:");
        panelClient.add(lab4);
        lab4.setSize(100, 20);
        lab4.setLocation(40, 140);
        lab4.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(poleUrodziny);
        poleUrodziny.setSize(200, 20);
        poleUrodziny.setLocation(160, 140);

        // ----------- MAIL --------------------
        JLabel lab5 = new JLabel("mail:");
        panelClient.add(lab5);
        lab5.setSize(100, 20);
        lab5.setLocation(40, 180);
        lab5.setHorizontalTextPosition(JLabel.RIGHT);
        panelClient.add(poleMail);
        poleMail.setSize(200, 20);
        poleMail.setLocation(160, 180);

        // ----------- ADRES ------------------
        JLabel lab6 = new JLabel("adres:");
        panelClient.add(lab6);
        lab6.setSize(100, 20);
        lab6.setLocation(40, 220);
        lab6.setHorizontalTextPosition(JLabel.RIGHT);
        panelClient.add(poleAdres);
        poleAdres.setSize(200, 20);
        poleAdres.setLocation(160, 220);

        // ----------- PHONE -------------------
        JLabel lab7 = new JLabel("telefon:");
        panelClient.add(lab7);
        lab7.setSize(100, 20);
        lab7.setLocation(40, 260);
        lab7.setHorizontalTextPosition(JLabel.RIGHT);
        panelClient.add(polePhone);
        polePhone.setSize(200, 20);
        polePhone.setLocation(160, 260);

        // ----------- CLIENT SAVE BUTTON ------------------
        panelClient.add(buttonSaveClient);
        buttonSaveClient.setSize(200, 20);
        buttonSaveClient.setLocation(160, 300);
        buttonSaveClient.addActionListener(akc_zap_kli);

        // ----------- CLIENT DELL BUTTON ------------------
        panelClient.add(buttonDellClient);
        buttonDellClient.setSize(200, 20);
        buttonDellClient.setLocation(400, 300);
        buttonDellClient.addActionListener(akc_usun_kli);

        // ----------- LIST CLIENT -------------------
        panelClient.add(scrollPaneClient);
        scrollPaneClient.setSize(200, 260);
        scrollPaneClient.setLocation(400, 20);
        listClient.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateClientList();



        // ----------- PANEL BOOKS --------
        panelBooks.setLayout(null);

        // ------------- ISBN ----------------
        JLabel labelISBN = new JLabel("ISBN:");
        panelBooks.add(labelISBN);
        labelISBN.setSize(100, 20);
        labelISBN.setLocation(40, 20);
        labelISBN.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleISBN);
        poleISBN.setSize(200, 20);
        poleISBN.setLocation(160, 20);

        // ------------- AUTOR ----------------
        JLabel labelAutor = new JLabel("autor:");
        panelBooks.add(labelAutor);
        labelAutor.setSize(100, 20);
        labelAutor.setLocation(40, 60);
        labelAutor.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleAutorBook);
        poleAutorBook.setSize(200, 20);
        poleAutorBook.setLocation(160, 60);

        // ------------- TYTUL ----------------
        JLabel labelTytul = new JLabel("tytul:");
        panelBooks.add(labelTytul);
        labelTytul.setSize(100, 20);
        labelTytul.setLocation(40, 100);
        labelTytul.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleTytulBook);
        poleTytulBook.setSize(200, 20);
        poleTytulBook.setLocation(160, 100);

        // ------------- TYP ----------------
        JLabel labelTyp = new JLabel("typ:");
        panelBooks.add(labelTyp);
        labelTyp.setSize(100, 20);
        labelTyp.setLocation(40, 140);
        labelTyp.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(comboTypBook);
        for (TypBook typBook: TypBook.values()) {
            comboTypBook.addItem(typBook);
        }
        comboTypBook.setSize(100,20);
        comboTypBook.setLocation(160,140);

        // ------------- WYDAWNICTWO ----------------
        JLabel labelWydaw = new JLabel("wydawnictwo:");
        panelBooks.add(labelWydaw);
        labelWydaw.setSize(100, 20);
        labelWydaw.setLocation(40, 180);
        labelWydaw.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleWydawBook);
        poleWydawBook.setSize(200, 20);
        poleWydawBook.setLocation(160, 180);

        // ------------- ROK ----------------
        JLabel labelRok = new JLabel("rok:");
        panelBooks.add(labelRok);
        labelRok.setSize(100, 20);
        labelRok.setLocation(40, 220);
        labelRok.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleRokBook);
        poleRokBook.setSize(200, 20);
        poleRokBook.setLocation(160, 220);

        // ------------- CENA ----------------
        JLabel labelCena = new JLabel("cena:");
        panelBooks.add(labelCena);
        labelCena.setSize(100, 20);
        labelCena.setLocation(40, 260);
        labelCena.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleCenaBook);
        poleCenaBook.setSize(200, 20);
        poleCenaBook.setLocation(160, 260);

        // ------------- LIST WITH BOOKS --------
        panelBooks.add(scrollPaneBooks);
        scrollPaneBooks.setSize(200, 260);
        scrollPaneBooks.setLocation(400, 20);
        listBooks.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateBooksList();

        // ------------- button to add book
        panelBooks.add(buttonSaveBook);
        buttonSaveBook.setSize(200, 20);
        buttonSaveBook.setLocation(160, 300);
        buttonSaveBook.addActionListener(akc_zap_ksia);
        // ------------- button to remove book
        panelBooks.add(buttonDellBook);
        buttonDellBook.setSize(200, 20);
        buttonDellBook.setLocation(400, 300);
        buttonDellBook.addActionListener(akc_usun_ksia);

        // ---------- NEW PRICE -----------
        JLabel labelNewPrice = new JLabel("nowa cena:");
        panelBooks.add(labelNewPrice);
        labelNewPrice.setSize(100, 20);
        labelNewPrice.setLocation(40, 330);
        labelNewPrice.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleNewPrice);
        poleNewPrice.setSize(200, 20);
        poleNewPrice.setLocation(160, 330);

        panelBooks.add(buttonNewPrice);
        buttonNewPrice.setSize(200, 20);
        buttonNewPrice.setLocation(400, 330);
        buttonNewPrice.addActionListener(chenge_price_book);




        // -----------PANEL ORDER --------
        panelOrder.setLayout(null);

        // ----------- Books list on Order
        JLabel labelBooksTitel = new JLabel("Książki:");
        panelOrder.add(labelBooksTitel);
        labelBooksTitel.setSize(100, 20);
        labelBooksTitel.setLocation(40, 20);
        labelBooksTitel.setHorizontalTextPosition(JLabel.RIGHT);


        panelOrder.add(scrollPaneBookOrder);
        scrollPaneBookOrder.setSize(350, 100);
        scrollPaneBookOrder.setLocation(40, 50);
        listBookOrder.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        updateBooksListOrder();


        // ----------- DATA ---------------
        JLabel labelDataOrder = new JLabel("data:");
        panelOrder.add(labelDataOrder );
        labelDataOrder.setSize(100, 20);
        labelDataOrder.setLocation(40, 160);
        labelDataOrder.setHorizontalTextPosition(JLabel.RIGHT);

        panelOrder.add(poleDataOrder);
        poleDataOrder.setSize(200, 20);
        poleDataOrder.setLocation(160, 160);

        // ------------- STATUS ----------------
        JLabel labelStatusOrder = new JLabel("status:");
        panelOrder.add(labelStatusOrder);
        labelStatusOrder.setSize(100, 20);
        labelStatusOrder.setLocation(40, 190);
        labelStatusOrder.setHorizontalTextPosition(JLabel.RIGHT);

        panelOrder.add(comboStatusOrder);
        comboStatusOrder.setSize(200, 20);
        comboStatusOrder.setLocation(160, 190);
        for (Status status: Status.values()) {
            comboStatusOrder.addItem(status);
        }

        // --------- Client List on Order -------

        JLabel labelClientTitel = new JLabel("Klient:");
        panelOrder.add(labelClientTitel);
        labelClientTitel.setSize(100, 20);
        labelClientTitel.setLocation(400, 20);
        labelClientTitel.setHorizontalTextPosition(JLabel.RIGHT);

        panelOrder.add(scrollPaneKlientOrder);
        scrollPaneKlientOrder.setSize(200, 130);
        scrollPaneKlientOrder.setLocation(400, 50);
        listKlientOrder.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateClientListOrder();

        panelOrder.add(buttonNewOrder);
        buttonNewOrder.setSize(200, 20);
        buttonNewOrder.setLocation(400, 190);
        buttonNewOrder.addActionListener(akc_add_order);

        // --------- ORDER LIST
        panelOrder.add(scrollPaneOrder);
        scrollPaneOrder.setSize(560, 110);
        scrollPaneOrder.setLocation(40, 220);
        listOrder.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateOrderList();

        // --------- NEW STATUS ORDER
        JLabel labelNewStatus = new JLabel("nowy status:");
        panelOrder.add(labelNewStatus);
        labelNewStatus.setSize(100, 20);
        labelNewStatus.setLocation(40, 340);
        labelNewStatus.setHorizontalTextPosition(JLabel.RIGHT);

        panelOrder.add(comboNewStatusOrder);
        comboNewStatusOrder.setSize(200, 20);
        comboNewStatusOrder.setLocation(160, 340);
        for (Status status: Status.values()) {
            comboNewStatusOrder.addItem(status);
        }

        panelOrder.add(buttonNewStatusOrder);
        buttonNewStatusOrder.setSize(200, 20);
        buttonNewStatusOrder.setLocation(400, 340);
        buttonNewStatusOrder.addActionListener(akc_change_status);







    }
}