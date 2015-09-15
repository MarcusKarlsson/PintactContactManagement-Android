package com.pinplanet.pintact.data;

import java.io.Serializable;

/**
 * User profile data object
 * <br/>
 * A profile is a child of a user record
 * <br/>
 * A profile has the following children (1:many):
 * <ul>
 *  <li>UserProfileAddress</li>
 *  <li>UserProfileAttribute</li>
 * </ul>
 * 
 */
public class UserProfile implements Serializable{

	/**
	 * Primary key for a profile
	 */
	private Long id;
	
	/**
	 * Link to parent user record
	 */
	private long userId;

	private String name;

	private String firstName;

	private String lastName;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  private String middleName;


  /**
	 * Title to associate with profile
	 */
	private String title;
	
	/**
	 * Company name to associate with profile
	 */
	private String companyName;
	
	/**
	 * Path to profile image on file system
	 */
	private String pathToImage;
	
	/**
	 * True if this address can be used to search for this profile
	 */
	private boolean searchable;
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getPathToImage() {
		return pathToImage;
	}

	public void setPathToImage(String pathToImage) {
		this.pathToImage = pathToImage;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}
	
	@Override
	public boolean equals(Object object) {
		boolean result = super.equals(object);
		if (!result) {
			return false;
		}
		UserProfile otherUserProfile = (UserProfile)object;
		if (userId != otherUserProfile.userId) {
		  return false;
		}
		if (name == null && otherUserProfile.name != null
        || (name != null && !name.equals(otherUserProfile.name))) {
		  return false;
		}
		return true;
	}

}
