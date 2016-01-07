package info.globalbus.dkim.spring;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import info.globalbus.dkim.DKIMSigner;
import info.globalbus.dkim.SMTPDKIMMessage;

public class DKIMJavaMailSenderProxy extends JavaMailSenderImpl {
	private DKIMSigner dkimSigner;
	private JavaMailSender javaMailSenderImpl;
	@Override
	protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
		MimeMessage[] signedMessages = new MimeMessage[mimeMessages.length];
		for(int i=0; i< mimeMessages.length;i++)
			signedMessages[i]=sign(mimeMessages[i]);
		super.doSend(signedMessages, originalMessages);
	}

	public DKIMSigner getDkimSigner() {
		return this.dkimSigner;
	}

	public void setDkimSigner(DKIMSigner dkimSigner) {
		this.dkimSigner = dkimSigner;
	}

	public JavaMailSender getJavaMailSenderImpl() {
		return this.javaMailSenderImpl;
	}

	public void setJavaMailSenderImpl(JavaMailSender javaMailSenderImpl) {
		this.javaMailSenderImpl = javaMailSenderImpl;
	}

	private MimeMessage sign(MimeMessage mimeMessage) {
		if (this.dkimSigner != null)
			try {
				return new SMTPDKIMMessage(mimeMessage, this.dkimSigner);
			} catch (MessagingException e) {
				throw new MailPreparationException(e);
			}
		return mimeMessage;
	}
}
