/**
 * 
 */
package com.tle.admin.controls.flickr.universal;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.common.wizard.controls.universal.handlers.FlickrSettings;

/**
 * Based on the YouTube plugin.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public class FlickrSettingsPanel extends com.tle.admin.controls.universal.UniversalControlSettingPanel
{
	/**
	 * Randomly generated serializable
	 */
	private static final long serialVersionUID = -3377112268177020761L;

	private final JTextField apiKey;
	private final JTextField apiSharedSecret;

	public FlickrSettingsPanel()
	{
		apiKey = new JTextField();
		apiSharedSecret = new JTextField();

		// Yuck, this seems to be the minimal amount of code to get a clickable
		// and styled link
		// (copied from the Cron link in the scheduler)
		JEditorPane apiKeyHelp = new JEditorPane();
		apiKeyHelp.setEditorKit(new HTMLEditorKit());
		apiKeyHelp.setDocument(new HTMLDocument());
		apiKeyHelp.setEditable(false);
		apiKeyHelp.setText("<html><head><style>" + "<!--a{color:#0000cc;text-decoration: none;}//--></style></head>"
			+ "<body>" + CurrentLocale.get("com.tle.admin.controls.flickr.settings.label.apikeyhelp"));
		apiKeyHelp.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
				{
					getClientService().showDocument(e.getURL(), null);
				}
			}
		});
		apiKeyHelp.setBackground(new JPanel().getBackground());

		add(apiKeyHelp, "span 2");

		add(new JLabel(CurrentLocale.get("com.tle.admin.controls.flickr.settings.label.apikey")));
		add(apiKey);
		add(new JLabel(CurrentLocale.get("com.tle.admin.controls.flickr.settings.label.apisharedsecret")));
		add(apiSharedSecret);
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.admin.controls.flickr.settings.title";
	}

	@Override
	public void load(UniversalSettings state)
	{
		FlickrSettings flickrSettings = new FlickrSettings(state);
		apiKey.setText(flickrSettings.getApiKey());
		apiSharedSecret.setText(flickrSettings.getApiSharedSecret());
	}

	@Override
	public void save(UniversalSettings state)
	{
		FlickrSettings flickrSettings = new FlickrSettings(state);
		flickrSettings.setApiKey(apiKey.getText().trim());
		flickrSettings.setApiSharedSecret(apiSharedSecret.getText().trim());
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// nfa
	}
}