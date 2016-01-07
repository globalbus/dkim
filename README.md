#DKIM for JavaMail

Fork of old unmaintained project from [Agitos.de](http://www.agitos.de/dkim-for-javamail/)

Code was cleaned and prepared to use with spring framework mail implementation. You can use `DKIMJavaMailSenderProxy` to replace default `JavaMailSenderImpl`.

Simple transparent configuration for `JavaMailSender`. privateKey is resource filename in this case
```
@Configuration
@PropertySources(value = { @PropertySource("classpath:/dkim.config"), @PropertySource("classpath:/smtp.cred") })
public class MailServiceConfig {
	@Value("${dkim.signinDomain}")
	String signingdomain;
	@Value("${dkim.selector}")
	String selector;
	@Value("${dkim.privateKey}")
	String privateKey;
	@Value("${smtp.host}")
	String host;
	@Value("${smtp.port}")
	int port;
	@Value("${smtp.username}")
	String username;
	@Value("${smtp.password}")
	String password;

	public Properties getJavaMailProperties() {
		Properties authProp = new Properties();
		authProp.put("mail.smtp.auth", true);
		authProp.put("mail.smtp.starttls.enable", true);
		return authProp;
	}
	
	public JavaMailSender configureMail() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(this.host);
		mailSender.setPort(this.port);
		mailSender.setUsername(this.username);
		mailSender.setPassword(this.password);
		mailSender.setJavaMailProperties(getJavaMailProperties());
		return mailSender;
	}

	public DKIMSigner getSigner() {
		if (StringUtils.isEmpty(this.privateKey))
			return null;
		Resource resource = new ClassPathResource(this.privateKey);
		try (BufferedInputStream bin = new BufferedInputStream(resource.getInputStream())) {
			byte[] raw = new byte[(int) resource.contentLength()];
			bin.read(raw, 0, (int) resource.contentLength());
			return new DKIMSigner(this.signingdomain, this.selector, raw);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Bean
	public JavaMailSender configureSignedMail(DKIMSigner signer) {
		DKIMJavaMailSenderProxy proxy = new DKIMJavaMailSenderProxy();
		proxy.setJavaMailSenderImpl(configureMail());
		proxy.setDkimSigner(signer);
		return proxy;
	}
```

Private Key must be stored in binary encoded form (DER Format). You can keep it as a file in jar, in secure store or other designated place.
Form more information about generating keys, see ORIGINAL_README.md

##How to check it works?
1. Generate keys
2. Add DKIM public key as an TXT record for your domain.
3. Send email from your domain to server, who checks DKIM, in example to GMail. If you see Authentication results like that, it works!
```
Authentication-Results:
       dkim=pass
```
4. Don't forget about SPF TXT Record
