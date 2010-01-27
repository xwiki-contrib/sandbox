package com.xpn.xwiki.watch.client.ui.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TextWidgetComposite extends Composite
{
    protected Widget mainWidget;
    protected FlowPanel widgetsPanel;
    protected Panel compositePanel;
    
    public TextWidgetComposite(Widget mainWidget)
    {
        this.mainWidget = mainWidget;
        this.widgetsPanel = new FlowPanel();
        
        compositePanel = new FlowPanel();
        compositePanel.add(this.mainWidget);
        compositePanel.add(this.widgetsPanel);
        this.updateCompositePanel();
        //every composite has to call initWidget in their constructors
        initWidget(compositePanel);
    }
    
    protected void updateCompositePanel() 
    {
        if (this.widgetsPanel == null || this.widgetsPanel.getWidgetCount() == 0) {
            this.compositePanel.remove(this.widgetsPanel);
        } else if(this.widgetsPanel.getParent() != this.compositePanel) {
            //add it to the composite panel
            this.compositePanel.add(this.widgetsPanel);
        }
    }
    
    public void add(TextWidgetComposite cText) {
        this.widgetsPanel.add(cText);
        //refresh composite panel
        this.updateCompositePanel();
    }
    
    public void remove(TextWidgetComposite cText) {
        this.widgetsPanel.remove(cText);
        //refresh composite panel
        this.updateCompositePanel();
    }
    
    public List getWidgets() {
        ArrayList widgets = new ArrayList();
        for (int i = 0; i < this.widgetsPanel.getWidgetCount(); i++) {
            widgets.add(this.widgetsPanel.getWidget(i));
        }
        return widgets;
    }

    public Widget getMainWidget()
    {
        return mainWidget;
    }
}
