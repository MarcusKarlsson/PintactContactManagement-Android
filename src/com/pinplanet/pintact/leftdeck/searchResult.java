package com.pinplanet.pintact.leftdeck;

public class searchResult {
	  private String name;
	  private String title;
	  private boolean selected;

	  public searchResult(String name, String title) {
	    this.name = name;
	    this.title = title;
	    selected = false;
	  }

	  public String getName() {
	    return name;
	  }

	  public void setName(String name) {
	    this.name = name;
	  }

	  public String getTitle() {
		    return title;
	  }

	  public void setTitle(String title) {
		    this.title = title;
	  }

	  public boolean isSelected() {
	    return selected;
	  }

	  public void setSelected(boolean selected) {
	    this.selected = selected;
	  }

}
