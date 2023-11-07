import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoDBConfig {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDBConfig() {
        MongoClientURI uri = new MongoClientURI("mongodb://localhost:27018");

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
            MongoClient.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        mongoClient = new MongoClient(uri);
        database = mongoClient.getDatabase("university").withCodecRegistry(codecRegistry);
    }

	public MongoDatabase getDatabase() {
		return database;
	}
}


