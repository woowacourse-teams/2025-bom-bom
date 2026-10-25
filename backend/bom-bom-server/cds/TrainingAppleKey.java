import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/**
 * CDS 학습 실행용 일회용 Apple 키(P-256)를 만들어 PKCS8 Base64로 출력한다.
 * 이미지 빌드마다 새로 만들고 버리므로 저장소에 개인키를 두지 않는다.
 */
public class TrainingAppleKey {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        byte[] privateKey = generator.generateKeyPair().getPrivate().getEncoded();
        System.out.print(Base64.getEncoder().encodeToString(privateKey));
    }
}
