

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

@WebServlet("/StudentCrud")
public class StudentCrud extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final MongoDBConfig mongoDBConfig;

    public StudentCrud() {
        super();
        mongoDBConfig = new MongoDBConfig();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String idParam = request.getParameter("id");

        ObjectMapper objectMapper = new ObjectMapper();
        MongoDatabase database = mongoDBConfig.getDatabase();
        MongoCollection<Student> collection = database.getCollection("students", Student.class);

        if (idParam != null) {
            try {
                ObjectId studentId = new ObjectId(idParam);
                Bson filter = Filters.eq("_id", studentId);
                Student student = collection.find(filter).first();

                if (student != null) {
                    String studentJson = objectMapper.writeValueAsString(student);
                    response.setContentType("application/json");
                    response.getWriter().print(studentJson);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            List<Student> students = collection.find().into(new ArrayList<>());
            String studentsJson = objectMapper.writeValueAsString(students);
            response.setContentType("application/json");
            response.getWriter().print(studentsJson);
        }
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MongoDatabase database = mongoDBConfig.getDatabase();
        MongoCollection<Student> collection = database.getCollection("students", Student.class);

        try {
            String jsonInput = request.getReader().lines().collect(Collectors.joining());
            Student student = objectMapper.readValue(jsonInput, Student.class);

            collection.insertOne(student);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
