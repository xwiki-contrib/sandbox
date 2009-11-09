package org.xwiki.platform.client;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * ContactWidget to keep a list of contact items. Needs to improve
 * 
 * @author tharindu
 */
public class ContactsWidget extends DecoratedStackPanel
{
    private VerticalPanel contactList = new VerticalPanel();

    protected ArrayList<Button> contactButtons = new ArrayList<Button>();

    /**
     * Creates an empty contacts widget
     */
    public ContactsWidget()
    {
        super();
        setWidth("200px");
        // contactList.setSpacing(4);
        // Add a list of contacts
        // String contactsHeader = getHeaderString("Contact List");
        // this.add(contactList, contactsHeader, true);
    }

    /**
     * Clear and loads the buttons into the panel
     */
    public void load()
    {
        contactList.clear();
        for (ButtonBase button : contactButtons) {
            createContactsItem(button.getTitle());
        }
        this.clear();
        this.add(contactList, getHeaderString("Tharindu Madushanka"), true);
    }

    /**
     * Adds a button with name to the contact list
     * 
     * @param name name of the contact to be added
     */
    public void add(String name)
    {
        Button myButton = new Button(name, contactButtonHandler);
        myButton.setTitle(name);
        contactButtons.add(myButton);
        load();
    }

    /**
     * Checks if the button with same name already added in contacts widget
     * 
     * @param name name of the contact
     * @return true if button with same name already exists
     */
    public boolean isAdded(String name)
    {
        for (ButtonBase button : contactButtons) {
            if (button.getTitle().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a string representation of the header that includes an image and some text.
     * 
     * @param text the header text
     * @param image the {@link AbstractImagePrototype} to add next to the header
     * @return the header as a string
     */
    private String getHeaderString(String text)
    {
        // Add the image and text to a horizontal panel
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setSpacing(0);
        hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hPanel.add(new Image("contactsgroup.gif"));
        HTML headerText = new HTML(text);
        headerText.setStyleName("contactWidgetHeader");
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(headerText);
        TextBox tb = new TextBox();
        tb.setStyleName("contactSearchBox");
        HTML statusText = new HTML("Available");
        statusText.setStyleName("contactStatusLabel");
        vPanel.add(tb);
        vPanel.add(statusText);
        hPanel.add(vPanel);
        // Return the HTML string for the panel
        return hPanel.getElement().getString();
    }

    private void createContactsItem(String name)
    {
        // Create a popup to show the contact info when a contact is clicked
        HorizontalPanel contactPopupContainer = new HorizontalPanel();
        contactPopupContainer.setSpacing(5);
        contactPopupContainer.add(new Image("defaultContact.jpg"));
        final HTML contactInfo = new HTML();
        contactPopupContainer.add(contactInfo);
        final PopupPanel contactPopup = new PopupPanel(true, false);
        contactPopup.setWidget(contactPopupContainer);

        final String contactName = name;
        final String contactEmail = removeSpaces(name.toLowerCase()) + "@xwiki.net";
        // final HTML contactLink = new HTML("<a href=\"javascript:undefined;\">" + contactName + "</a>");
        final Label contactLink = new Label(name);
        contactLink.setStyleName("contactLabel");
        contactLink.setWidth("200px");
        contactLink.setHeight("30px");
        contactList.add(contactLink);

        // Open the contact info popup when the user clicks a contact
        contactLink.addMouseListener(new MouseListener()
        {

            public void onMouseDown(Widget widget, int x, int y)
            {
            }

            public void onMouseEnter(Widget widget)
            {
            }

            public void onMouseLeave(Widget widget)
            {
                contactLink.setStyleName("contactLabel");
                contactPopup.hide();
            }

            public void onMouseMove(Widget widget, int x, int y)
            {
                contactLink.setStyleName("contactLabelHover");
                // Set the info about the contact
                contactInfo.setHTML(contactName + "<br><i>" + contactEmail + "</i>");

                // Show the popup of contact info
                int left = contactLink.getAbsoluteLeft() + 190;
                int top = contactLink.getAbsoluteTop() - 15;
                contactPopup.setPopupPosition(left, top);
                contactPopup.show();
            }

            public void onMouseUp(Widget widget, int x, int y)
            {
            }
        });

    }

    private String removeSpaces(String s)
    {
        String[] choppedUpString = s.trim().split(" ");
        String trimmedString = "";
        for (int i = 0; i < choppedUpString.length; i++) {
            trimmedString = trimmedString + choppedUpString[i];
        }
        return trimmedString;
    }

    ClickListener contactButtonHandler = new ClickListener()
    {
        public void onClick(Widget widget)
        {
            if (widget != null && widget instanceof Button) {
                String contactTitle = widget.getTitle();
                System.out.println("[ContactsWidget] ClickHandler onClick() name: " + contactTitle);
                // Use the title that has the name in it to do what you need to do
            } else {
                // TODO: Some error
                System.out.println("[ContactsWidget] ClickHandler onClick() non button widget clicked");
            }
        }
    };

}
