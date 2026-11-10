import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Main {

    // =========================
    // DATABASE CONFIGURATION
    // =========================

    static final String DB_URL = "jdbc:mysql://localhost:3306/lost_found_db";
    static final String DB_USER = "root";
    // IMPORTANT:
    // Put your own MySQL root password here
    static final String DB_PASSWORD = "Riyaz@2007";

    // =========================
    // CURRENT USER
    // =========================

    static int currentUserId = -1;
    static String currentUserName = "";
    static String currentUserRole = "";

    // =========================
    // COLORS
    // =========================

    static final Color BLUE = new Color(33, 97, 140);
    static final Color GREEN = new Color(39, 174, 96);
    static final Color RED = new Color(192, 57, 43);
    static final Color ORANGE = new Color(230, 126, 34);
    static final Color DARK = new Color(44, 62, 80);
    static final Color PURPLE = new Color(142, 68, 173);

    // =========================
    // MAIN
    // =========================

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            ensureAdminAccount();

            SwingUtilities.invokeLater(() -> showLogin());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Database connection error:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // DATABASE CONNECTION
    // =========================

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD
        );
    }

    // =========================
    // CREATE ADMIN ACCOUNT
    // =========================

    static void ensureAdminAccount() {

        String sql =
                "INSERT INTO users (name, email, password, role) " +
                "VALUES ('Administrator', 'admin@lostfound.com', '1234', 'admin') " +
                "ON DUPLICATE KEY UPDATE password='1234', role='admin'";

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Admin account setup failed:\n" + e.getMessage()
            );
        }
    }

    // =========================================================
    // COMMON FRAME
    // =========================================================

    static void maximizeFrame(JFrame frame) {
        frame.setSize(1000, 700);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    static void showLogin() {

        JFrame frame = new JFrame("Lost & Found - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        maximizeFrame(frame);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(500, 480));
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 210, 210)
                        ),
                        BorderFactory.createEmptyBorder(
                                30, 40, 30, 40
                        )
                )
        );
        card.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("LOST & FOUND");
        title.setFont(new Font("Arial", Font.BOLD, 34));
        title.setForeground(BLUE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        JLabel subtitle = new JLabel("Management System");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        card.add(subtitle, gbc);

        gbc.gridwidth = 1;

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 16));

        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(emailLabel, gbc);

        JTextField emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(250, 40));

        gbc.gridx = 1;
        card.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));

        gbc.gridx = 0;
        gbc.gridy = 3;
        card.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 40));

        gbc.gridx = 1;
        card.add(passwordField, gbc);

        JButton loginButton = createButton(
                "LOGIN",
                BLUE
        );

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        card.add(loginButton, gbc);

        JButton registerButton = createButton(
                "CREATE ACCOUNT",
                GREEN
        );

        gbc.gridy = 5;
        card.add(registerButton, gbc);

        JLabel adminHint = new JLabel(
                "Admin: admin@lostfound.com / 1234"
        );

        adminHint.setHorizontalAlignment(SwingConstants.CENTER);
        adminHint.setForeground(Color.GRAY);

        gbc.gridy = 6;
        card.add(adminHint, gbc);

        root.add(card);

        frame.add(root);

        loginButton.addActionListener(e -> {

            String email = emailField.getText().trim();
            String password =
                    new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter email and password."
                );

                return;
            }

            String sql =
                    "SELECT id, name, password, role " +
                    "FROM users WHERE email=?";

            try (
                    Connection con = getConnection();
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ps.setString(1, email);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    String dbPassword =
                            rs.getString("password");

                    if (dbPassword.equals(password)) {

                        currentUserId =
                                rs.getInt("id");

                        currentUserName =
                                rs.getString("name");

                        currentUserRole =
                                rs.getString("role");

                        frame.dispose();

                        showDashboard();

                    } else {

                        JOptionPane.showMessageDialog(
                                frame,
                                "Invalid password!"
                        );
                    }

                } else {

                    JOptionPane.showMessageDialog(
                            frame,
                            "User not found!"
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Login error:\n" +
                        ex.getMessage()
                );
            }
        });

        registerButton.addActionListener(e -> {

            frame.dispose();
            showRegistration();

        });

        frame.setVisible(true);
    }

    // =========================================================
    // REGISTRATION
    // =========================================================

    static void showRegistration() {

        JFrame frame =
                new JFrame("Lost & Found - Registration");

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel(new GridBagLayout());

        card.setPreferredSize(
                new Dimension(550, 550)
        );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 210, 210)
                        ),
                        BorderFactory.createEmptyBorder(
                                30, 40, 30, 40
                        )
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(10, 10, 10, 10);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel title =
                new JLabel("CREATE ACCOUNT");

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        32
                )
        );

        title.setForeground(GREEN);
        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        card.add(title, gbc);

        gbc.gridwidth = 1;

        JLabel nameLabel =
                new JLabel("Name");

        nameLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 1;

        card.add(nameLabel, gbc);

        JTextField nameField =
                new JTextField();

        nameField.setPreferredSize(
                new Dimension(260, 40)
        );

        gbc.gridx = 1;

        card.add(nameField, gbc);

        JLabel emailLabel =
                new JLabel("Email");

        emailLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 2;

        card.add(emailLabel, gbc);

        JTextField emailField =
                new JTextField();

        emailField.setPreferredSize(
                new Dimension(260, 40)
        );

        gbc.gridx = 1;

        card.add(emailField, gbc);

        JLabel passwordLabel =
                new JLabel("Password");

        passwordLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 3;

        card.add(passwordLabel, gbc);

        JPasswordField passwordField =
                new JPasswordField();

        passwordField.setPreferredSize(
                new Dimension(260, 40)
        );

        gbc.gridx = 1;

        card.add(passwordField, gbc);

        JButton registerButton =
                createButton(
                        "REGISTER",
                        GREEN
                );

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;

        card.add(registerButton, gbc);

        JButton backButton =
                createButton(
                        "BACK TO LOGIN",
                        DARK
                );

        gbc.gridy = 5;

        card.add(backButton, gbc);

        root.add(card);

        frame.add(root);

        registerButton.addActionListener(e -> {

            String name =
                    nameField.getText().trim();

            String email =
                    emailField.getText().trim();

            String password =
                    new String(
                            passwordField.getPassword()
                    );

            if (
                    name.isEmpty() ||
                    email.isEmpty() ||
                    password.isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please fill all fields."
                );

                return;
            }

            String sql =
                    "INSERT INTO users " +
                    "(name,email,password,role) " +
                    "VALUES (?,?,?,'student')";

            try (
                    Connection con = getConnection();
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        frame,
                        "Registration successful!"
                );

                frame.dispose();

                showLogin();

            } catch (SQLIntegrityConstraintViolationException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Email already exists!"
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Registration error:\n" +
                        ex.getMessage()
                );
            }
        });

        backButton.addActionListener(e -> {

            frame.dispose();
            showLogin();

        });

        frame.setVisible(true);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    static void showDashboard() {

        JFrame frame =
                new JFrame("Lost & Found - Dashboard");

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root =
                new JPanel(new GridBagLayout());

        root.setBackground(
                new Color(245, 247, 250)
        );

        JPanel mainPanel =
                new JPanel(new GridBagLayout());

        mainPanel.setBackground(Color.WHITE);

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        30, 50, 30, 50
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(10, 15, 10, 15);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel title =
                new JLabel(
                        "LOST & FOUND DASHBOARD"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        38
                )
        );

        title.setForeground(BLUE);

        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        mainPanel.add(title, gbc);

        JLabel welcome =
                new JLabel(
                        "Welcome, " +
                        currentUserName +
                        " (" +
                        currentUserRole +
                        ")"
                );

        welcome.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        20
                )
        );

        welcome.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        gbc.gridy = 1;

        mainPanel.add(welcome, gbc);

        gbc.gridwidth = 1;

        JButton lostButton =
                createButton(
                        "Report Lost Item",
                        BLUE
                );

        JButton foundButton =
                createButton(
                        "Report Found Item",
                        GREEN
                );

        JButton searchButton =
                createButton(
                        "Search Items",
                        ORANGE
                );

        JButton reportsButton =
                createButton(
                        "My Reports",
                        BLUE
                );

        JButton claimButton =
                createButton(
                        "Claim Lost Item",
                        RED
                );

        JButton myClaimsButton =
                createButton(
                        "My Claims",
                        PURPLE
                );

        JButton logoutButton =
                createButton(
                        "Logout",
                        DARK
                );

        gbc.gridx = 0;
        gbc.gridy = 2;

        mainPanel.add(lostButton, gbc);

        gbc.gridx = 1;

        mainPanel.add(foundButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;

        mainPanel.add(searchButton, gbc);

        gbc.gridx = 1;

        mainPanel.add(reportsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;

        mainPanel.add(claimButton, gbc);

        gbc.gridx = 1;

        mainPanel.add(myClaimsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;

        mainPanel.add(logoutButton, gbc);

        if (
                currentUserRole
                        .equalsIgnoreCase("admin")
        ) {

            JButton adminButton =
                    createButton(
                            "Admin - View Claims",
                            PURPLE
                    );

            gbc.gridy = 6;

            mainPanel.add(
                    adminButton,
                    gbc
            );

            adminButton.addActionListener(
                    e -> showAdminClaims()
            );
        }

        root.add(mainPanel);

        frame.add(root);

        lostButton.addActionListener(
                e -> showReportItem("Lost")
        );

        foundButton.addActionListener(
                e -> showReportItem("Found")
        );

        searchButton.addActionListener(
                e -> showSearchItems()
        );

        reportsButton.addActionListener(
                e -> showMyReports()
        );

        claimButton.addActionListener(
                e -> showClaimItem()
        );

        myClaimsButton.addActionListener(
                e -> showMyClaims()
        );

        logoutButton.addActionListener(e -> {

            currentUserId = -1;
            currentUserName = "";
            currentUserRole = "";

            frame.dispose();

            showLogin();
        });

        frame.setVisible(true);
    }

    // =========================================================
    // REPORT LOST / FOUND
    // =========================================================

    static void showReportItem(String type) {

        JFrame frame =
                new JFrame(
                        "Report " + type + " Item"
                );

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root =
                new JPanel(new GridBagLayout());

        root.setBackground(
                new Color(245, 247, 250)
        );

        JPanel card =
                new JPanel(new GridBagLayout());

        card.setBackground(Color.WHITE);

        card.setPreferredSize(
                new Dimension(650, 650)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 210, 210)
                        ),
                        BorderFactory.createEmptyBorder(
                                25, 35, 25, 35
                        )
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(7, 8, 7, 8);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel title =
                new JLabel(
                        "REPORT " +
                        type.toUpperCase() +
                        " ITEM"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        30
                )
        );

        title.setForeground(
                type.equals("Lost")
                        ? RED
                        : GREEN
        );

        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        card.add(title, gbc);

        gbc.gridwidth = 1;

        JLabel itemLabel =
                new JLabel("Item Name");

        JTextField itemField =
                new JTextField();

        JLabel categoryLabel =
                new JLabel("Category");

        JTextField categoryField =
                new JTextField();

        JLabel descriptionLabel =
                new JLabel("Description");

        JTextField descriptionField =
                new JTextField();

        JLabel locationLabel =
                new JLabel("Location");

        JTextField locationField =
                new JTextField();

        JLabel dateLabel =
                new JLabel("Date (YYYY-MM-DD)");

        JTextField dateField =
                new JTextField();

        JLabel contactLabel =
                new JLabel("Contact");

        JTextField contactField =
                new JTextField();

        JLabel[] labels = {
                itemLabel,
                categoryLabel,
                descriptionLabel,
                locationLabel,
                dateLabel,
                contactLabel
        };

        JTextField[] fields = {
                itemField,
                categoryField,
                descriptionField,
                locationField,
                dateField,
                contactField
        };

        for (int i = 0; i < labels.length; i++) {

            labels[i].setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            15
                    )
            );

            fields[i].setPreferredSize(
                    new Dimension(280, 38)
            );

            gbc.gridx = 0;
            gbc.gridy = i + 1;

            card.add(labels[i], gbc);

            gbc.gridx = 1;

            card.add(fields[i], gbc);
        }

        JButton submitButton =
                createButton(
                        "SUBMIT REPORT",
                        type.equals("Lost")
                                ? RED
                                : GREEN
                );

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;

        card.add(submitButton, gbc);

        JButton backButton =
                createButton(
                        "BACK",
                        DARK
                );

        gbc.gridy = 8;

        card.add(backButton, gbc);

        root.add(card);

        frame.add(root);

        submitButton.addActionListener(e -> {

            String itemName =
                    itemField.getText().trim();

            String category =
                    categoryField.getText().trim();

            String description =
                    descriptionField.getText().trim();

            String location =
                    locationField.getText().trim();

            String date =
                    dateField.getText().trim();

            String contact =
                    contactField.getText().trim();

            if (
                    itemName.isEmpty() ||
                    category.isEmpty() ||
                    location.isEmpty() ||
                    date.isEmpty() ||
                    contact.isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please fill all required fields."
                );

                return;
            }

            String sql =
                    "INSERT INTO items " +
                    "(user_id,item_name,category," +
                    "description,location,lost_date," +
                    "contact,type,status) " +
                    "VALUES (?,?,?,?,?,?,?,?,'Active')";

            try (
                    Connection con = getConnection();
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ps.setInt(
                        1,
                        currentUserId
                );

                ps.setString(
                        2,
                        itemName
                );

                ps.setString(
                        3,
                        category
                );

                ps.setString(
                        4,
                        description
                );

                ps.setString(
                        5,
                        location
                );

                ps.setDate(
                        6,
                        Date.valueOf(date)
                );

                ps.setString(
                        7,
                        contact
                );

                ps.setString(
                        8,
                        type
                );

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        frame,
                        type +
                        " item reported successfully!"
                );

                frame.dispose();

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Date must be YYYY-MM-DD."
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Error:\n" +
                        ex.getMessage()
                );
            }
        });

        backButton.addActionListener(
                e -> frame.dispose()
        );

        frame.setVisible(true);
    }

    // =========================================================
    // SEARCH ITEMS
    // =========================================================

    static void showSearchItems() {

        JFrame frame =
                new JFrame("Search Items");

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root =
                new JPanel(new BorderLayout(10, 10));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30
                )
        );

        JPanel topPanel =
                new JPanel(new FlowLayout(
                        FlowLayout.CENTER,
                        10,
                        10
                ));

        JLabel searchLabel =
                new JLabel("Search:");

        searchLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        JTextField searchField =
                new JTextField(25);

        JButton searchButton =
                createButton(
                        "SEARCH",
                        ORANGE
                );

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);

        root.add(
                topPanel,
                BorderLayout.NORTH
        );

        String[] columns = {
                "ID",
                "Item",
                "Category",
                "Description",
                "Location",
                "Date",
                "Contact",
                "Type",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(28);
        table.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );

        JScrollPane scrollPane =
                new JScrollPane(table);

        root.add(
                scrollPane,
                BorderLayout.CENTER
        );

        JButton backButton =
                createButton(
                        "BACK",
                        DARK
                );

        JPanel bottomPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        bottomPanel.add(backButton);

        root.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        frame.add(root);

        Runnable searchAction = () -> {

            String keyword =
                    searchField.getText().trim();

            String sql =
                    "SELECT id,item_name,category," +
                    "description,location,lost_date," +
                    "contact,type,status " +
                    "FROM items " +
                    "WHERE item_name LIKE ? " +
                    "OR category LIKE ? " +
                    "OR location LIKE ? " +
                    "ORDER BY id DESC";

            model.setRowCount(0);

            try (
                    Connection con = getConnection();
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                String value =
                        "%" + keyword + "%";

                ps.setString(1, value);
                ps.setString(2, value);
                ps.setString(3, value);

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    model.addRow(
                            new Object[]{
                                    rs.getInt("id"),
                                    rs.getString(
                                            "item_name"
                                    ),
                                    rs.getString(
                                            "category"
                                    ),
                                    rs.getString(
                                            "description"
                                    ),
                                    rs.getString(
                                            "location"
                                    ),
                                    rs.getDate(
                                            "lost_date"
                                    ),
                                    rs.getString(
                                            "contact"
                                    ),
                                    rs.getString(
                                            "type"
                                    ),
                                    rs.getString(
                                            "status"
                                    )
                            }
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Search error:\n" +
                        ex.getMessage()
                );
            }
        };

        searchButton.addActionListener(
                e -> searchAction.run()
        );

        backButton.addActionListener(
                e -> frame.dispose()
        );

        searchAction.run();

        frame.setVisible(true);
    }

    // =========================================================
    // MY REPORTS
    // =========================================================

    static void showMyReports() {

        JFrame frame =
                new JFrame("My Reports");

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root =
                new JPanel(new BorderLayout(10, 10));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30
                )
        );

        JLabel title =
                new JLabel(
                        "MY REPORTS"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );

        title.setForeground(BLUE);
        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        root.add(
                title,
                BorderLayout.NORTH
        );

        String[] columns = {
                "ID",
                "Item",
                "Category",
                "Description",
                "Location",
                "Date",
                "Contact",
                "Type",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(28);

        root.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JButton backButton =
                createButton(
                        "BACK",
                        DARK
                );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        bottom.add(backButton);

        root.add(
                bottom,
                BorderLayout.SOUTH
        );

        frame.add(root);

        String sql =
                "SELECT id,item_name,category," +
                "description,location,lost_date," +
                "contact,type,status " +
                "FROM items " +
                "WHERE user_id=? " +
                "ORDER BY id DESC";

        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    currentUserId
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                model.addRow(
                        new Object[]{
                                rs.getInt("id"),
                                rs.getString(
                                        "item_name"
                                ),
                                rs.getString(
                                        "category"
                                ),
                                rs.getString(
                                        "description"
                                ),
                                rs.getString(
                                        "location"
                                ),
                                rs.getDate(
                                        "lost_date"
                                ),
                                rs.getString(
                                        "contact"
                                ),
                                rs.getString(
                                        "type"
                                ),
                                rs.getString(
                                        "status"
                                )
                        }
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    frame,
                    "Error:\n" +
                    ex.getMessage()
            );
        }

        backButton.addActionListener(
                e -> frame.dispose()
        );

        frame.setVisible(true);
    }

    // =========================================================
    // CLAIM LOST ITEM
    // =========================================================

    static void showClaimItem() {

        JFrame frame =
                new JFrame("Claim Lost Item");

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root =
                new JPanel(new BorderLayout(10, 10));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30
                )
        );

        JPanel top =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        JLabel label =
                new JLabel(
                        "Search Lost Item:"
                );

        JTextField searchField =
                new JTextField(25);

        JButton searchButton =
                createButton(
                        "SEARCH",
                        RED
                );

        top.add(label);
        top.add(searchField);
        top.add(searchButton);

        root.add(
                top,
                BorderLayout.NORTH
        );

        String[] columns = {
                "ID",
                "Item",
                "Category",
                "Description",
                "Location",
                "Date",
                "Contact",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(28);

        root.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        JButton claimButton =
                createButton(
                        "CLAIM SELECTED ITEM",
                        RED
                );

        JButton backButton =
                createButton(
                        "BACK",
                        DARK
                );

        bottom.add(claimButton);
        bottom.add(backButton);

        root.add(
                bottom,
                BorderLayout.SOUTH
        );

        frame.add(root);

        Runnable searchAction = () -> {

            String keyword =
                    searchField.getText().trim();

            String sql =
                    "SELECT id,item_name,category," +
                    "description,location,lost_date," +
                    "contact,status " +
                    "FROM items " +
                    "WHERE type='Lost' " +
                    "AND status <> 'Claimed' " +
                    "AND item_name LIKE ? " +
                    "ORDER BY id DESC";

            model.setRowCount(0);

            try (
                    Connection con = getConnection();
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ps.setString(
                        1,
                        "%" + keyword + "%"
                );

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    model.addRow(
                            new Object[]{
                                    rs.getInt("id"),
                                    rs.getString(
                                            "item_name"
                                    ),
                                    rs.getString(
                                            "category"
                                    ),
                                    rs.getString(
                                            "description"
                                    ),
                                    rs.getString(
                                            "location"
                                    ),
                                    rs.getDate(
                                            "lost_date"
                                    ),
                                    rs.getString(
                                            "contact"
                                    ),
                                    rs.getString(
                                            "status"
                                    )
                            }
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Search error:\n" +
                        ex.getMessage()
                );
            }
        };

        searchButton.addActionListener(
                e -> searchAction.run()
        );

        claimButton.addActionListener(e -> {

            int selectedRow =
                    table.getSelectedRow();

            if (selectedRow == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please select an item."
                );

                return;
            }

            int itemId =
                    Integer.parseInt(
                            model.getValueAt(
                                    selectedRow,
                                    0
                            ).toString()
                    );

            String itemName =
                    model.getValueAt(
                            selectedRow,
                            1
                    ).toString();

            String message =
                    JOptionPane.showInputDialog(
                            frame,
                            "Why do you believe this is your item?"
                    );

            if (
                    message == null ||
                    message.trim().isEmpty()
            ) {
                return;
            }

            String checkSql =
                    "SELECT COUNT(*) FROM claims " +
                    "WHERE item_id=? " +
                    "AND user_id=? " +
                    "AND status='Pending'";

            String insertSql =
                    "INSERT INTO claims " +
                    "(item_id,user_id,claim_message,status) " +
                    "VALUES (?,?,?,'Pending')";

            try (
                    Connection con = getConnection();
                    PreparedStatement check =
                            con.prepareStatement(checkSql)
            ) {

                check.setInt(1, itemId);
                check.setInt(
                        2,
                        currentUserId
                );

                ResultSet rs =
                        check.executeQuery();

                rs.next();

                int count =
                        rs.getInt(1);

                if (count > 0) {

                    JOptionPane.showMessageDialog(
                            frame,
                            "You already have a pending claim for this item."
                    );

                    return;
                }

                try (
                        PreparedStatement insert =
                                con.prepareStatement(
                                        insertSql
                                )
                ) {

                    insert.setInt(
                            1,
                            itemId
                    );

                    insert.setInt(
                            2,
                            currentUserId
                    );

                    insert.setString(
                            3,
                            message
                    );

                    insert.executeUpdate();

                    JOptionPane.showMessageDialog(
                            frame,
                            "Claim submitted for " +
                            itemName
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Claim error:\n" +
                        ex.getMessage()
                );
            }
        });

        backButton.addActionListener(
                e -> frame.dispose()
        );

        searchAction.run();

        frame.setVisible(true);
    }

    // =========================================================
    // MY CLAIMS
    // =========================================================

    static void showMyClaims() {

        JFrame frame =
                new JFrame("My Claims");

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root =
                new JPanel(new BorderLayout(10, 10));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30
                )
        );

        JLabel title =
                new JLabel(
                        "MY CLAIMS"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );

        title.setForeground(PURPLE);

        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        root.add(
                title,
                BorderLayout.NORTH
        );

        String[] columns = {
                "Claim ID",
                "Item",
                "Category",
                "Location",
                "Claim Message",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(28);

        root.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JButton backButton =
                createButton(
                        "BACK",
                        DARK
                );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        bottom.add(backButton);

        root.add(
                bottom,
                BorderLayout.SOUTH
        );

        frame.add(root);

        String sql =
                "SELECT c.id, i.item_name, " +
                "i.category, i.location, " +
                "c.claim_message, c.status " +
                "FROM claims c " +
                "JOIN items i ON c.item_id=i.id " +
                "WHERE c.user_id=? " +
                "ORDER BY c.id DESC";

        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    currentUserId
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                model.addRow(
                        new Object[]{
                                rs.getInt("id"),
                                rs.getString(
                                        "item_name"
                                ),
                                rs.getString(
                                        "category"
                                ),
                                rs.getString(
                                        "location"
                                ),
                                rs.getString(
                                        "claim_message"
                                ),
                                rs.getString(
                                        "status"
                                )
                        }
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    frame,
                    "Error:\n" +
                    ex.getMessage()
            );
        }

        backButton.addActionListener(
                e -> frame.dispose()
        );

        frame.setVisible(true);
    }

    // =========================================================
    // ADMIN CLAIMS
    // =========================================================

    static void showAdminClaims() {

        JFrame frame =
                new JFrame(
                        "Admin - View Claims"
                );

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        maximizeFrame(frame);

        JPanel root =
                new JPanel(new BorderLayout(10, 10));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30
                )
        );

        JLabel title =
                new JLabel(
                        "ADMIN - CLAIM MANAGEMENT"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        28
                )
        );

        title.setForeground(PURPLE);

        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        root.add(
                title,
                BorderLayout.NORTH
        );

        String[] columns = {
                "Claim ID",
                "Item ID",
                "Item",
                "User",
                "Email",
                "Claim Message",
                "Claim Status",
                "Item Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        JTable table =
                new JTable(model);

        table.setRowHeight(30);

        root.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        JButton approveButton =
                createButton(
                        "APPROVE CLAIM",
                        GREEN
                );

        JButton rejectButton =
                createButton(
                        "REJECT CLAIM",
                        RED
                );

        JButton refreshButton =
                createButton(
                        "REFRESH",
                        BLUE
                );

        JButton backButton =
                createButton(
                        "BACK",
                        DARK
                );

        bottom.add(approveButton);
        bottom.add(rejectButton);
        bottom.add(refreshButton);
        bottom.add(backButton);

        root.add(
                bottom,
                BorderLayout.SOUTH
        );

        frame.add(root);

        Runnable loadClaims = () -> {

            model.setRowCount(0);

            String sql =
                    "SELECT c.id AS claim_id, " +
                    "i.id AS item_id, " +
                    "i.item_name, " +
                    "u.name AS user_name, " +
                    "u.email, " +
                    "c.claim_message, " +
                    "c.status AS claim_status, " +
                    "i.status AS item_status " +
                    "FROM claims c " +
                    "JOIN items i " +
                    "ON c.item_id=i.id " +
                    "JOIN users u " +
                    "ON c.user_id=u.id " +
                    "ORDER BY c.id DESC";

            try (
                    Connection con = getConnection();
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    model.addRow(
                            new Object[]{
                                    rs.getInt(
                                            "claim_id"
                                    ),
                                    rs.getInt(
                                            "item_id"
                                    ),
                                    rs.getString(
                                            "item_name"
                                    ),
                                    rs.getString(
                                            "user_name"
                                    ),
                                    rs.getString(
                                            "email"
                                    ),
                                    rs.getString(
                                            "claim_message"
                                    ),
                                    rs.getString(
                                            "claim_status"
                                    ),
                                    rs.getString(
                                            "item_status"
                                    )
                            }
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Error:\n" +
                        ex.getMessage()
                );
            }
        };

        approveButton.addActionListener(e -> {

            int row =
                    table.getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please select a claim."
                );

                return;
            }

            int claimId =
                    Integer.parseInt(
                            model.getValueAt(
                                    row,
                                    0
                            ).toString()
                    );

            int itemId =
                    Integer.parseInt(
                            model.getValueAt(
                                    row,
                                    1
                            ).toString()
                    );

            String claimStatus =
                    model.getValueAt(
                            row,
                            6
                    ).toString();

            if (
                    !claimStatus.equalsIgnoreCase(
                            "Pending"
                    )
            ) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Only pending claims can be approved."
                );

                return;
            }

            String claimSql =
                    "UPDATE claims " +
                    "SET status='Approved' " +
                    "WHERE id=?";

            String itemSql =
                    "UPDATE items " +
                    "SET status='Claimed' " +
                    "WHERE id=?";

            try (
                    Connection con = getConnection()
            ) {

                con.setAutoCommit(false);

                try (
                        PreparedStatement ps1 =
                                con.prepareStatement(
                                        claimSql
                                );

                        PreparedStatement ps2 =
                                con.prepareStatement(
                                        itemSql
                                )
                ) {

                    ps1.setInt(
                            1,
                            claimId
                    );

                    ps1.executeUpdate();

                    ps2.setInt(
                            1,
                            itemId
                    );

                    ps2.executeUpdate();

                    con.commit();

                    JOptionPane.showMessageDialog(
                            frame,
                            "Claim approved!"
                    );

                } catch (Exception ex) {

                    con.rollback();

                    throw ex;
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Approval error:\n" +
                        ex.getMessage()
                );
            }

            loadClaims.run();
        });

        rejectButton.addActionListener(e -> {

            int row =
                    table.getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please select a claim."
                );

                return;
            }

            int claimId =
                    Integer.parseInt(
                            model.getValueAt(
                                    row,
                                    0
                            ).toString()
                    );

            String claimStatus =
                    model.getValueAt(
                            row,
                            6
                    ).toString();

            if (
                    !claimStatus.equalsIgnoreCase(
                            "Pending"
                    )
            ) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Only pending claims can be rejected."
                );

                return;
            }

            String sql =
                    "UPDATE claims " +
                    "SET status='Rejected' " +
                    "WHERE id=?";

            try (
                    Connection con = getConnection();
                    PreparedStatement ps =
                            con.prepareStatement(sql)
            ) {

                ps.setInt(
                        1,
                        claimId
                );

                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        frame,
                        "Claim rejected!"
                );

                loadClaims.run();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Rejection error:\n" +
                        ex.getMessage()
                );
            }
        });

        refreshButton.addActionListener(
                e -> loadClaims.run()
        );

        backButton.addActionListener(
                e -> frame.dispose()
        );

        loadClaims.run();

        frame.setVisible(true);
    }

    // =========================================================
    // COMMON BUTTON
    // =========================================================

    static JButton createButton(
            String text,
            Color color
    ) {

        JButton button =
                new JButton(text);

        button.setPreferredSize(
                new Dimension(
                        280,
                        50
                )
        );

        button.setBackground(color);

        button.setForeground(Color.WHITE);

        button.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        15
                )
        );

        button.setFocusPainted(false);

        return button;
    }
}