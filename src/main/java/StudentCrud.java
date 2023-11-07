

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

@WebServlet("/StudentCrud")
public class StudentCrud extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final MongoDBConfig mongoDBConfig;
	private ObjectMapper objectMapper;
    private Logger logger;


    public StudentCrud() {
        mongoDBConfig = new MongoDBConfig();
        objectMapper = new ObjectMapper();
        logger = LoggerFactory.getLogger(StudentCrud.class);
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String idParam = request.getParameter("id");

        if (idParam != null) {
            try {
                ObjectId studentId = new ObjectId(idParam);
                Bson filter = Filters.eq("_id", studentId);
                Student student = getCollection("students").find(filter).first();

                if (student != null) {
                    String studentJson = objectMapper.writeValueAsString(student);
                    response.setContentType("application/json");
                    response.getWriter().print(studentJson);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    logger.warn("Student not found for ID: {}", idParam);
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.error("Error in doGet", e);
            }
        } else {
            List<Student> students = getCollection("students").find().into(new ArrayList<>());
            String studentsJson = objectMapper.writeValueAsString(students);
            response.setContentType("application/json");
            response.getWriter().print(studentsJson);
        }
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String jsonInput = request.getReader().lines().collect(Collectors.joining());
            Student student = objectMapper.readValue(jsonInput, Student.class);

            getCollection("students").insertOne(student);
            
            logger.info("Created a new student: {}", student.getId());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Error in doPost", e);
        }
    }
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            ObjectId studentId = new ObjectId(idParam);

            Student updatedFields = objectMapper.readValue(request.getReader(), Student.class);

            Bson filter = Filters.eq("_id", studentId);
            Student existingStudent = getCollection("students").find(filter).first();

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

                UpdateResult updateResult = getCollection("students").updateOne(filter, update);

                if (updateResult.getModifiedCount() > 0) {
                	response.setStatus(HttpServletResponse.SC_OK);
                	logger.info("Updated student with ID: {}", studentId);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                logger.warn("Student not found for update with ID: {}", studentId);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Error in doPut", e);
        }
    }
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String idParam = request.getParameter("id");
	    try {
	        if (idParam != null) {
	            ObjectId studentId = new ObjectId(idParam);
	            Bson filter = Filters.eq("_id", studentId);
	            Student existingStudent = getCollection("students").find(filter).first();

	            if (existingStudent != null) {
	                DeleteResult deleteResult = getCollection("students").deleteOne(filter);

	                if (deleteResult.getDeletedCount() > 0) {
	                    response.setStatus(HttpServletResponse.SC_OK);
	                    logger.info("Deleted student with ID: {}", studentId);
	                } else {
	                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	                }
	            } else {
	                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	                logger.warn("Student not found for delete with ID: {}", studentId);
	            }
	        } else {
	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            logger.warn("Invalid request for student deletion without ID");
	        }
	    } catch (Exception e) {
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        logger.error("Error in doDelete", e);
	    }
	}

	
	private MongoCollection<Student> getCollection(String name){
		return mongoDBConfig.getDatabase().getCollection(name, Student.class);
	}


}
