package Food;
import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;

@WebServlet("/FoodServlet")
public class FoodServlet extends HttpServlet {

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/fooddb", "root", "ShriSwami@25");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html; charset=UTF-8");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        String action = req.getParameter("action");
        if (action == null) action = "view";

        try {
            switch (action) {

                case "publicView":
                    viewMenuPublic(req, out);
                    break;

                case "edit":
                    showEditForm(req, out);
                    break;
                case "delete":
                    deleteItem(req, res);
                    break;
                case "updated":
                    showSuccessPage(out, "✅ Item Updated Successfully!");
                    break;
                case "deleted":
                    showSuccessPage(out, "🗑 Item Deleted Successfully!");
                    break;
                default:
                    viewItems(out);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div class='alert alert-danger'>Error: " + e.getMessage() + "</div>");
        }
    }


    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if ("add".equals(action)) {
            addItem(req, res);
        } else if ("update".equals(action)) {
            updateItem(req, res);
        }
    }

    private void viewItems(PrintWriter out) throws Exception {
        Connection con = getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM food_items ORDER BY id ASC");

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">\">");
        out.println("<title>Food Menu</title>");
        out.println("</head><body class='bg-light'>");

        out.println("<div class='container py-5'>");
        out.println("<h2 class='text-center text-success mb-4'>🍽 Rucha's Food Menu 🍽</h2>");

        out.println("<table class='table table-bordered table-striped text-center'>");
        out.println("<thead class='table-dark'><tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Action</th></tr></thead>");
        out.println("<tbody>");

        int count = 1;
        while (rs.next()) {
            out.println("<tr>");
            out.println("<td>" + (count++) + "</td>");
            out.println("<td>" + rs.getString("name") + "</td>");
            out.println("<td>" + rs.getString("category") + "</td>");
            out.println("<td>" + rs.getDouble("price") + "</td>");
            out.println("<td>"
                    + "<a href='FoodServlet?action=edit&id=" + rs.getInt("id") + "'>✏️ Edit</a> | "
                    + "<a href='FoodServlet?action=delete&id=" + rs.getInt("id") + "' onclick='return confirm(\"Delete this item?\")'>🗑 Delete</a>"
                    + "</td>");
            out.println("</tr>");
        }

        out.println("</tbody></table>");

        out.println("<div class='text-center mt-4'>");
        out.println("<a href='food.html' class='btn btn-success me-3'>➕ Add New Item</a>");
        out.println("</div>");

        out.println("</div></body></html>");
        con.close();
    }


 
    private void viewMenuPublic(HttpServletRequest req, PrintWriter out) throws Exception {

        Connection con = getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM food_items ORDER BY id ASC");

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<link rel='stylesheet' href='css/bootstrap.min.css'>");
        out.println("<title>Menu Card</title>");

        out.println("<style>");
        out.println("body{background:#f8f9fa;}");
        out.println(".card{border-radius:15px;}");
        out.println("</style>");
        out.println("</head>");

        out.println("<body class='bg-light'><div class='container py-4'>");
        out.println("<h2 class='text-center text-primary mb-4'>📋 Rucha's Food Menu</h2>");

        out.println("<div class='row'>");

        while (rs.next()) {
            out.println("<div class='col-md-4 mb-3'>");
            out.println("<div class='card shadow-sm p-3'>");
            out.println("<h5>" + rs.getString("name") + "</h5>");
            out.println("<p class='text-muted'>" + rs.getString("category") + "</p>");
            out.println("<h6 class='text-success fw-bold'>₹ " + rs.getDouble("price") + "</h6>");
            out.println("</div></div>");
        }

        out.println("</div></div></body></html>");

        con.close();
    }



    private void addItem(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        String name = req.getParameter("name");
        String category = req.getParameter("category");
        String priceStr = req.getParameter("price");

        if (!name.matches("[a-zA-Z ]+") || !category.matches("[a-zA-Z ]+")) {

            res.setContentType("text/html; charset=UTF-8");
            PrintWriter out = res.getWriter();

            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">");
            out.println("<title>Error</title></head><body class='bg-light'>");

            out.println("<div class='container py-5 text-center'>");
            out.println("<div class='alert alert-danger fs-5'>❌ Invalid Input! Only letters allowed.</div>");
            out.println("<a href='food.html' class='btn btn-warning mt-3'>⬅ Try Again</a>");
            out.println("</div></body></html>");
            return;
        }

        double price = Double.parseDouble(priceStr);

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO food_items (name, category, price) VALUES (?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, price);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        showSuccessPage(out, "✅ Item Inserted Successfully!");
    }


    private void showEditForm(HttpServletRequest req, PrintWriter out) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));

        Connection con = getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM food_items WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">");
        out.println("<title>Edit Item</title></head>");

        out.println("<body class='bg-light'><div class='container py-5'>");

        if (rs.next()) {
            out.println("<div class='card p-4 shadow' style='max-width:500px;margin:auto;'>");
            out.println("<h4 class='text-center mb-3'>✏ Edit Food Item</h4>");
            out.println("<form action='FoodServlet' method='post'>");
            out.println("<input type='hidden' name='action' value='update'>");
            out.println("<input type='hidden' name='id' value='" + id + "'>");
            out.println("<div class='mb-3'><label>Name</label>");
            out.println("<input type='text' class='form-control' name='name' value='" + rs.getString("name") + "' required></div>");

            out.println("<div class='mb-3'><label>Category</label>");
            out.println("<input type='text' class='form-control' name='category' value='" + rs.getString("category") + "' required></div>");

            out.println("<div class='mb-3'><label>Price</label>");
            out.println("<input type='number' step='0.01' class='form-control' name='price' value='" + rs.getDouble("price") + "' required></div>");

            out.println("<button type='submit' class='btn btn-success w-100'>💾 Update</button>");
            out.println("</form></div>");
        }

        out.println("</div></body></html>");

        con.close();
    }


    private void updateItem(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        String name = req.getParameter("name");
        String category = req.getParameter("category");
        double price = Double.parseDouble(req.getParameter("price"));

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE food_items SET name=?, category=?, price=? WHERE id=?");
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, price);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        res.sendRedirect("FoodServlet?action=updated");
    }


    private void deleteItem(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        int id = Integer.parseInt(req.getParameter("id"));

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM food_items WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        res.sendRedirect("FoodServlet?action=deleted");
    }


    private void showSuccessPage(PrintWriter out, String message) {
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">");
        out.println("<title>Success</title></head>");
        out.println("<body class='bg-light'><div class='container py-5 text-center'>");
        out.println("<div class='alert alert-success fs-5'>" + message + "</div>");
        out.println("<a href='FoodServlet?action=view' class='btn btn-primary mt-3'>⬅ Back</a>");
        out.println("</div></body></html>");
    }
}
