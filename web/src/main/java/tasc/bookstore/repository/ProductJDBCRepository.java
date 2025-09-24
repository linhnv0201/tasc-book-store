package tasc.bookstore.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ProductJDBCRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findProductsByCategoryOrderByPriceDesc(Long categoryId) {
        String sql = "CALL get_products_search_by_category_id_and_order_by_price_desc(:categoryId)";
        Map<String, Object> params = Map.of("categoryId", categoryId);
//        return jdbcTemplate.query(sql, params, (rs) -> {
//            List<Map<String, Object>> results = new ArrayList<>();
//            while (rs.next()) {
//                Map<String, Object> row = new HashMap<>();
//                row.put("name", rs.getString("name"));
//                row.put("author", rs.getString("author"));
//                row.put("description", rs.getString("description"));
//                row.put("price", rs.getBigDecimal("price"));
//                row.put("stock", rs.getInt("stock"));
//                row.put("sold_quantity", rs.getInt("sold_quantity"));
//                row.put("category_name", rs.getString("name")); // nếu cột trùng tên thì cần alias trong SP
//                results.add(row);
//            }
//            return results;
//        });
        return namedJdbcTemplate.queryForList(sql, params);
    }

    public List<Map<String, Object>> findProductWithCategoriesById(Long id) {
        String sql = """
                        SELECT p.id AS product_id,
                       p.name AS product_name,
                       p.price,
                       p.cost,
                       c.name AS category_name
                FROM products p
                LEFT JOIN product_categories pc ON p.id = pc.product_id
                LEFT JOIN categories c ON pc.category_id = c.id
                WHERE p.id = :id
                """;

        Map<String, Object> params = Map.of("id", id);

        return namedJdbcTemplate.queryForList(sql, params);
    }

//    public List<Map<String, Object>> findProductsByAuthor(String author) {
//        String sql = """
//        SELECT
//            p.id AS product_id,
//            p.name AS product_name,
//            p.author,
//            p.description,
//            p.price,
//            p.stock,
//            c.name AS category_name
//        FROM products p
//        LEFT JOIN product_categories pc ON p.id = pc.product_id
//        LEFT JOIN categories c ON pc.category_id = c.id
//        WHERE p.author = ?
//        ORDER BY p.name ASC
//    """;
//
//        // queryForList trả về List<Map<String, Object>>
//        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, author);
//
//        // Gom các category cho mỗi product
//        Map<Long, Map<String, Object>> products = new LinkedHashMap<>();
//        for (Map<String, Object> row : rows) {
//            Long productId = ((Number) row.get("product_id")).longValue();
//
//            Map<String, Object> productMap = products.computeIfAbsent(productId, id -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("product_id", productId);
//                map.put("product_name", row.get("product_name"));
//                map.put("author", row.get("author"));
//                map.put("description", row.get("description"));
//                map.put("price", row.get("price"));
//                map.put("stock", row.get("stock"));
//                map.put("categories", new LinkedHashSet<String>());
//                return map;
//            });
//
//            Set<String> categories = (Set<String>) productMap.get("categories");
//            String categoryName = (String) row.get("category_name");
//            if (categoryName != null) {
//                categories.add(categoryName);
//            }
//        }
//
//        return new ArrayList<>(products.values());
//    }

    public List<Map<String, Object>> getTopSoldProducts(LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
        SELECT p.id, p.name, SUM(oi.quantity) AS total_sold
        FROM order_items oi
        JOIN products p ON oi.product_id = p.id
        JOIN orders o ON oi.order_id = o.id
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (startDate != null && endDate != null) {
            sql.append(" WHERE o.created_at BETWEEN :startDate AND :endDate");
            params.addValue("startDate", startDate);
            params.addValue("endDate", endDate);
        } else if (startDate != null && endDate == null) {
            sql.append(" WHERE o.created_at >= :startDate AND o.created_at <= NOW()");
            params.addValue("startDate", startDate);
        } else if (startDate == null && endDate != null) {
            sql.append(" WHERE o.created_at <= :endDate");
            params.addValue("endDate", endDate);
        } // nếu cả hai null => all time -> không WHERE

        sql.append(" GROUP BY p.id, p.name ORDER BY total_sold DESC");

        return namedJdbcTemplate.query(sql.toString(), params,
                (ResultSet rs) -> {
                    List<Map<String, Object>> results = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("name", rs.getString("name"));
                        row.put("total_sold", rs.getInt("total_sold"));
                        results.add(row);
                    }
                    return results;
                }
        );
    }



    public List<Map<String, Object>> getPurchaseOrderItemBySupplierId(
            Long supplierId, LocalDate from, LocalDate to) {

        // Nếu to == null thì lấy thời điểm hiện tại
        if (to == null) {
            to = LocalDate.now();
        }

        String sql = "SELECT " +
                "    p.id AS product_id, " +
                "    p.name AS product_name, " +
                "    SUM(poi.quantity) AS total_quantity " +
                "FROM purchase_order_items poi " +
                "JOIN purchase_orders po ON poi.purchase_order_id = po.id " +
                "JOIN products p ON poi.product_id = p.id " +
                "WHERE po.supplier_id = ? " +
                "  AND po.created_at BETWEEN ? AND ? " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_quantity DESC";

        return jdbcTemplate.queryForList(sql, supplierId, from, to);
    }

}
