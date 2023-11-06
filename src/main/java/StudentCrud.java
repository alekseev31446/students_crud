

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

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
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MongoDatabase database = mongoDBConfig.getDatabase();
        MongoCollection<Student> collection = database.getCollection("students", Student.class);

        try {
            String idParam = request.getParameter("id");
            ObjectId studentId = new ObjectId(idParam);

            Student updatedFields = objectMapper.readValue(request.getReader(), Student.class);

            Bson filter = Filters.eq("_id", studentId);
            Student existingStudent = collection.find(filter).first();

            if (existingStudent != null) {
                BsonDocument updateDoc = new BsonDocument();

                if (updatedFields.getFirstName() != null) {
                    updateDoc.put("firstName", new BsonString(updatedFields.getFirstName()));
                }
                if (updatedFields.getLastName() != null) {
                    updateDoc.put("lastName", new BsonString(updatedFields.getLastName()));
                }
                if (updatedFields.getAge() > 0) {
                    updateDoc.put("age", new BsonInt32(updatedFields.getAge()));
                }
                if (updatedFields.getDepartment() != null) {
                    updateDoc.put("department", new BsonString(updatedFields.getDepartment()));
                }

                Bson update = new BsonDocument("$set", updateDoc);

                UpdateResult updateResult = collection.updateOne(filter, update);

                if (updateResult.getModifiedCount() > 0) {
                	response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam != null) {
            ObjectId studentId = new ObjectId(idParam);
            MongoDatabase database = mongoDBConfig.getDatabase();
            MongoCollection<Student> collection = database.getCollection("students", Student.class);
            Bson filter = Filters.eq("_id", studentId);
            Student existingStudent = collection.find(filter).first();

            if (existingStudent != null) {
                DeleteResult deleteResult = collection.deleteOne(filter);

                if (deleteResult.getDeletedCount() > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


}
