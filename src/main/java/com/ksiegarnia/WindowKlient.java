package com.ksiegarnia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

class WindowKlient extends JFrame {
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
    private JTextField poleDataZamow = new JTextField();
    private JComboBox comboTypZamow = new JComboBox();

    private DefaultListModel<String> listModelKlientZamow = new DefaultListModel<>();
    private JList<String> listKlientZamow = new JList<>(listModelKlientZamow);
    private JScrollPane scrollPaneKlientZamow = new JScrollPane(listKlientZamow);


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
                JOptionPane.showMessageDialog(WindowKlient.this, "błąd w polu z peselm");
                polePesel.setText("");
                polePesel.requestFocus();
                return;
            }

            String imie = poleImia.getText();
            String nazwisko = poleNazwisko.getText();
            String ur = poleUrodziny.getText();
            if (! isDataValid(ur)) {
                JOptionPane.showMessageDialog(WindowKlient.this, "Data [rok-miesiec-dzien] np.1987-04-30");
                poleUrodziny.setText("");
                poleUrodziny.requestFocus();
                return;
            }
            if (imie.equals("") || nazwisko.equals("") || ur.equals("")) {
                JOptionPane.showMessageDialog(WindowKlient.this, "nie wypełnione pole z imieniem lub nazwiskiem lub datą urodzenia");
                return;
            }

            String mail = poleMail.getText();
            /*     //--- validation for mail ---
            if(! mail.matches("[A-Z0-9._%+-]@[A-Z0-9.-].[A-Z]{2,6}")){
                JOptionPane.showMessageDialog(WindowKlient.this,"Prosze podac korektny mail adres");
                poleMail.setText("");
                poleMail.requestFocus();
                return;
            }*/
            String adr = poleAdres.getText();
            String tel = polePhone.getText();
            if (mail.equals("") || adr.equals("") || tel.equals("")) {
                JOptionPane.showMessageDialog(WindowKlient.this, "nie wypełnione pole z emailem lub adresem");
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
    private void apdateBooksList() {
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
                JOptionPane.showMessageDialog(WindowKlient.this, "błąd w polu z ISBN");
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
                JOptionPane.showMessageDialog(WindowKlient.this, "nie wypełnione pole z awtorem, tytulem lub typem książki");
                return;
            }

            String wydaw = poleWydawBook.getText();
            String rok = poleRokBook.getText();
            String cena = poleCenaBook.getText();
            if (cena.equals("")) {
                JOptionPane.showMessageDialog(WindowKlient.this, "nie wypełnione pole z ceną");
                return;
            }

            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
                Statement stmt = conn.createStatement();
                String sqlInsertKsia = "INSERT INTO `ksiazki`(`isbn`, `autor`, `tytul`, `typ`, `wydawnictwo`, `rok`, `cena`) VALUES ('" + ISBN + "','" + autor + "','" + tytul + "','" + typ + "','" + wydaw + "','" + rok + "','" + cena + "')";
                int res = stmt.executeUpdate(sqlInsertKsia);
                if (res == 1) {
                    log.setText("OK - książka dodana do bazy");
                    apdateBooksList();
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
                    apdateBooksList();
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
                String sql = "UPDATE `ksiazki` SET `cena`='" + newPrice + "' WHERE `isbn`='" + p + "'";

                int res = stmt.executeUpdate(sql);
                if (res == 1) {
                    log.setText("OK - cena książki zmieniona");
                    apdateBooksList();
                } else {
                    log.setText("nie zmieniono cenę ksiazki");
                }
            } catch (SQLException ex) {
                log.setText("błąd SQL - nie ununięto ksiazke");
            }
        }
    };


    //function to update date on list CLIENT on panel ORDER
    private void updateClientListOrder() {
        try (Connection conn= DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT klienci.pesel, nazwisko, imie FROM klienci ORDER BY nazwisko, imie";
            ResultSet res = stmt.executeQuery(sql);
            listModelKlientZamow.clear();
            while(res.next()) {
                String s = res.getString(1) + ": " + res.getString(2) + " " + res.getString(3);
                listModelKlientZamow.addElement(s);
            }
        }
        catch (SQLException ex) {
            log.setText("nie udało się zaktualizować listy klientów");
        }
    }


    public WindowKlient() throws SQLException {
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
        lab1.setLocation(40, 40);
        lab1.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(polePesel);
        polePesel.setSize(200, 20);
        polePesel.setLocation(160, 40);

        // ----------- IMIE -----------------
        JLabel lab2 = new JLabel("imię:");
        panelClient.add(lab2);
        lab2.setSize(100, 20);
        lab2.setLocation(40, 80);
        lab2.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(poleImia);
        poleImia.setSize(200, 20);
        poleImia.setLocation(160, 80);

        // ----------- NAZWISKO ---------------
        JLabel lab3 = new JLabel("nazwisko:");
        panelClient.add(lab3);
        lab3.setSize(100, 20);
        lab3.setLocation(40, 120);
        lab3.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(poleNazwisko);
        poleNazwisko.setSize(200, 20);
        poleNazwisko.setLocation(160, 120);

        // ----------- DATE ---------------------
        JLabel lab4 = new JLabel("data urodzenia:");
        panelClient.add(lab4);
        lab4.setSize(100, 20);
        lab4.setLocation(40, 160);
        lab4.setHorizontalTextPosition(JLabel.RIGHT);

        panelClient.add(poleUrodziny);
        poleUrodziny.setSize(200, 20);
        poleUrodziny.setLocation(160, 160);

        // ----------- MAIL --------------------
        JLabel lab5 = new JLabel("mail:");
        panelClient.add(lab5);
        lab5.setSize(100, 20);
        lab5.setLocation(40, 200);
        lab5.setHorizontalTextPosition(JLabel.RIGHT);
        panelClient.add(poleMail);
        poleMail.setSize(200, 20);
        poleMail.setLocation(160, 200);

        // ----------- ADRES ------------------
        JLabel lab6 = new JLabel("adres:");
        panelClient.add(lab6);
        lab6.setSize(100, 20);
        lab6.setLocation(40, 240);
        lab6.setHorizontalTextPosition(JLabel.RIGHT);
        panelClient.add(poleAdres);
        poleAdres.setSize(200, 20);
        poleAdres.setLocation(160, 240);

        // ----------- PHONE -------------------
        JLabel lab7 = new JLabel("telefon:");
        panelClient.add(lab7);
        lab7.setSize(100, 20);
        lab7.setLocation(40, 280);
        lab7.setHorizontalTextPosition(JLabel.RIGHT);
        panelClient.add(polePhone);
        polePhone.setSize(200, 20);
        polePhone.setLocation(160, 280);

        // ----------- CLIENT SAVE BUTTON ------------------
        panelClient.add(buttonSaveClient);
        buttonSaveClient.setSize(200, 20);
        buttonSaveClient.setLocation(160, 320);
        buttonSaveClient.addActionListener(akc_zap_kli);

        // ----------- CLIENT DELL BUTTON ------------------
        panelClient.add(buttonDellClient);
        buttonDellClient.setSize(200, 20);
        buttonDellClient.setLocation(400, 320);
        buttonDellClient.addActionListener(akc_usun_kli);

        // ----------- LIST CLIENT -------------------
        panelClient.add(scrollPaneClient);
        scrollPaneClient.setSize(200, 260);
        scrollPaneClient.setLocation(400, 40);
        listClient.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateClientList();



        // ----------- PANEL BOOKS --------
        panelBooks.setLayout(null);

        // ------------- ISBN ----------------
        JLabel labelISBN = new JLabel("ISBN:");
        panelBooks.add(labelISBN);
        labelISBN.setSize(100, 20);
        labelISBN.setLocation(40, 40);
        labelISBN.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleISBN);
        poleISBN.setSize(200, 20);
        poleISBN.setLocation(160, 40);

        // ------------- AUTOR ----------------
        JLabel labelAutor = new JLabel("autor:");
        panelBooks.add(labelAutor);
        labelAutor.setSize(100, 20);
        labelAutor.setLocation(40, 80);
        labelAutor.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleAutorBook);
        poleAutorBook.setSize(200, 20);
        poleAutorBook.setLocation(160, 80);

        // ------------- TYTUL ----------------
        JLabel labelTytul = new JLabel("tytul:");
        panelBooks.add(labelTytul);
        labelTytul.setSize(100, 20);
        labelTytul.setLocation(40, 120);
        labelTytul.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleTytulBook);
        poleTytulBook.setSize(200, 20);
        poleTytulBook.setLocation(160, 120);

        // ------------- TYP ----------------
        JLabel labelTyp = new JLabel("typ:");
        panelBooks.add(labelTyp);
        labelTyp.setSize(100, 20);
        labelTyp.setLocation(40, 160);
        labelTyp.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(comboTypBook);
        for (TypBook typBook: TypBook.values()) {
            comboTypBook.addItem(typBook);
        }
        comboTypBook.setSize(100,20);
        comboTypBook.setLocation(160,160);

        // ------------- WYDAWNICTWO ----------------
        JLabel labelWydaw = new JLabel("wydawnictwo:");
        panelBooks.add(labelWydaw);
        labelWydaw.setSize(100, 20);
        labelWydaw.setLocation(40, 200);
        labelWydaw.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleWydawBook);
        poleWydawBook.setSize(200, 20);
        poleWydawBook.setLocation(160, 200);

        // ------------- ROK ----------------
        JLabel labelRok = new JLabel("rok:");
        panelBooks.add(labelRok);
        labelRok.setSize(100, 20);
        labelRok.setLocation(40, 240);
        labelRok.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleRokBook);
        poleRokBook.setSize(200, 20);
        poleRokBook.setLocation(160, 240);

        // ------------- CENA ----------------
        JLabel labelCena = new JLabel("cena:");
        panelBooks.add(labelCena);
        labelCena.setSize(100, 20);
        labelCena.setLocation(40, 280);
        labelCena.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleCenaBook);
        poleCenaBook.setSize(200, 20);
        poleCenaBook.setLocation(160, 280);

        // ------------- LIST WITH BOOKS --------
        panelBooks.add(scrollPaneBooks);
        scrollPaneBooks.setSize(200, 260);
        scrollPaneBooks.setLocation(400, 40);
        listBooks.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apdateBooksList();

        // ------------- button to add book
        panelBooks.add(buttonSaveBook);
        buttonSaveBook.setSize(200, 20);
        buttonSaveBook.setLocation(160, 320);
        buttonSaveBook.addActionListener(akc_zap_ksia);
        // ------------- button to remove book
        panelBooks.add(buttonDellBook);
        buttonDellBook.setSize(200, 20);
        buttonDellBook.setLocation(400, 320);
        buttonDellBook.addActionListener(akc_usun_ksia);

        // ---------- NEW PRICE -----------
        JLabel labelNewPrice = new JLabel("nowa cena:");
        panelBooks.add(labelNewPrice);
        labelNewPrice.setSize(100, 20);
        labelNewPrice.setLocation(40, 350);
        labelNewPrice.setHorizontalTextPosition(JLabel.RIGHT);

        panelBooks.add(poleNewPrice);
        poleNewPrice.setSize(200, 20);
        poleNewPrice.setLocation(160, 350);

        panelBooks.add(buttonNewPrice);
        buttonNewPrice.setSize(200, 20);
        buttonNewPrice.setLocation(400, 350);
        buttonNewPrice.addActionListener(chenge_price_book);




        // -----------PANEL ORDER --------
        panelOrder.setLayout(null);

        JLabel labelDataZamow = new JLabel("data:");
        panelOrder.add(labelDataZamow );
        labelDataZamow.setSize(100, 20);
        labelDataZamow.setLocation(40, 40);
        labelDataZamow.setHorizontalTextPosition(JLabel.RIGHT);

        panelOrder.add(poleDataZamow);
        poleDataZamow.setSize(200, 20);
        poleDataZamow.setLocation(160, 40);

        // ------------- AUTOR ----------------
        JLabel labelTypZamow = new JLabel("typ:");
        panelOrder.add(labelTypZamow);
        labelTypZamow.setSize(100, 20);
        labelTypZamow.setLocation(40, 80);
        labelTypZamow.setHorizontalTextPosition(JLabel.RIGHT);

        panelOrder.add(comboTypZamow);
        comboTypZamow.setSize(200, 20);
        comboTypZamow.setLocation(160, 80);
        for (Status status: Status.values()) {
            comboTypZamow.addItem(status);
        }

        // --------- Client ComboBox -------

        panelOrder.add(scrollPaneKlientZamow);
        scrollPaneKlientZamow.setSize(200, 260);
        scrollPaneKlientZamow.setLocation(400, 40);
        listKlientZamow.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateClientListOrder();


    }
}