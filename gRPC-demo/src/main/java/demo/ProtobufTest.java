package demo;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProtobufTest {
    public static void main(String[] args) throws IOException {
        // 创建 Person 对象
        PersonProto.Person person = PersonProto.Person.newBuilder()
                .setName("Alice")
                .setId(1)
                .setEmail("alice@example.com")
                .build();

        // 序列化
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        person.writeTo(output);
        byte[] personBytes = output.toByteArray();

        // 反序列化
        PersonProto.Person personDeserialized = PersonProto.Person.parseFrom(new ByteArrayInputStream(personBytes));

        // 输出检验
        System.out.println("Name: " + personDeserialized.getName());
        System.out.println("ID: " + personDeserialized.getId());
        System.out.println("Email: " + personDeserialized.getEmail());
    }
}
