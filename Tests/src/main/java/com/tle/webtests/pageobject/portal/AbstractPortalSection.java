package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.WaitingPageObject;

public abstract class AbstractPortalSection<T extends AbstractPortalSection<T>> extends AbstractPage<T>
{
	protected String title;

	public AbstractPortalSection(PageContext context, String title)
	{
		super(context);
		this.title = title;
	}

	protected WebElement getBoxHead()
	{
		return find(driver, By.xpath("//h3[normalize-space(text())="+quoteXPath(getTitle())+"]/ancestor::div[contains(@class, 'box_head')][1]"));
	}

	protected WebElement getBoxContent()
	{
		return find(driver, By.xpath("//h3[normalize-space(text())="+quoteXPath(getTitle())+"]/ancestor::div[contains(@class, 'box_head')][1]/following-sibling::div[contains(@class, 'box_content')]/div"));
	}

	protected WebElement findLoadedElement()
	{
		return getBoxHead();
	}

	public HomePage delete()
	{
		ExpectedCondition<Boolean> removalCondition = removalCondition(getBoxHead());
		showButtons();
		getBoxHead().findElement(By.className("box_close")).click();
		acceptConfirmation();
		waiter.until(removalCondition);
		return new HomePage(context).get();
	}

	public T minMax()
	{
		boolean present = isPresent(getBoxContent());
		WaitingPageObject<T> aWaiter;
		if( present )
		{
			aWaiter = removalWaiter(getBoxContent());
		}
		else
		{
			aWaiter = visibilityWaiter(getBoxContent());
		}

		getBoxHead().findElement(By.className("box_minimise")).click();
		return aWaiter.get();
	}

	public boolean isMinimisable()
	{
		return isPresent(getBoxHead(), By.className("box_minimise"));
	}

	public boolean isCloseable()
	{
		return isPresent(getBoxHead(), By.className("box_close"));
	}

	public <P extends AbstractPortalEditPage<P>> P edit(P portal)
	{
		showButtons();
		getBoxHead().findElement(By.className("box_edit")).click();
		return portal.get();
	}

	private void showButtons()
	{
		// hover doesn't work correctly so just force the buttons to show
		((JavascriptExecutor) driver).executeScript("$('img.action').show();");
	}

	public String getTitle()
	{
		return title;
	}
}