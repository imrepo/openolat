package org.olat.modules.fo;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.util.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={"classpath:/org/olat/modules/fo/textServiceMock.xml"})

@RunWith(SpringJUnit4ClassRunner.class)
public class WordCountTest {
	
	@Autowired
	TextService languageService;
	
	
	@Test
	public void testCleanMessage() {
		Filter filter = new QuoteAndTagFilter();
		String text = "<p>&nbsp;</p><div class=\"b_quote_wrapper\"><div class=\"b_quote_author mceNonEditable\">Am 23.11.09 12:29 hat OLAT Administrator geschrieben:</div><blockquote class=\"b_quote\"><p>Quelques mots que je voulais &eacute;crire. Et encore un ou deux.</p></blockquote></div><p>Et une r&eacute;ponse avec citation incorpor&eacute;e</p>";
		String output = filter.filter(text);
		assertTrue("  Et une réponse avec citation incorporée".equals(output));
	}
	
	/**
	 * Test pass if the detection is better as 80%
	 */
	@Test
	public void testDetectLanguage() {
		double count = 0;
		for(TestTextCase.Text text:TestTextCase.getCases()) {
			Locale locale = languageService.detectLocale(text.getText());
			if(locale != null && locale.getLanguage().equals(text.getLanguage())) {
				count++;
			}
		}
		double ratio = count / TestTextCase.getCases().length;
		assertTrue(ratio > 0.8d);
	}
	
	@Test
	public void testWordCount() {
		for(TestTextCase.Text text:TestTextCase.getCases()) {
			Locale locale = new Locale(text.getLanguage());
			int words = languageService.wordCount(text.getText(), locale);
			assertTrue(words == text.getWords());
		}
	}
	
	@Test
	public void testCharacterCount() {
		for(TestTextCase.Text text:TestTextCase.getCases()) {
			Locale locale = new Locale(text.getLanguage());
			int characters = languageService.characterCount(text.getText(), locale);
			assertTrue(characters == text.getCharacters());
		}
	}
}