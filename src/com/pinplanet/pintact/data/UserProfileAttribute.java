package com.pinplanet.pintact.data;


import java.io.Serializable;

/**
 * An attribute tied to a user profile
 * 
 */
public class UserProfileAttribute implements Serializable{

    public UserProfileAttribute(){

    }

    public UserProfileAttribute(AttributeType attributeType, String label, String value)
    {
        this.type = attributeType;
        this.label =label;
        this.value = value;
    }

	/**
	 * Primary key for a profile attribute
	 */
	private long id;
	
	/**
	 * Link to parent profile record
	 */
	private long userProfileId;
	
	/**
	 * Link to parent user record
	 */
	private long userId;
	
	/**
	 * Type of attribute
	 */
	private AttributeType type;
	
	/**
	 * Label identifying the type of attribute (e.g., mobile, Twitter, etc.)
	 */
	private String label;
	
	/**
	 * True if this attribute can be used to search for this profile
	 */
	private boolean searchable;
	
	/**
	 * Value of the attribute
	 */
	private String value;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getUserProfileId() {
		return userProfileId;
	}

	public void setUserProfileId(long userProfileId) {
		this.userProfileId = userProfileId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
  public boolean equals(Object object) {
		
		//return true;
		/*
    boolean result = super.equals(object);
    
    if (!result) {
      return false;
    }
    */
    UserProfileAttribute otherUserProfileAttribute = (UserProfileAttribute)object;
    if (type != otherUserProfileAttribute.type) {
        return false;
      }
    if ((value == null && otherUserProfileAttribute.value != null)
            || (value != null && !value.equals(otherUserProfileAttribute.value))) {
          return false;
        }
    return true;
        
    /*
    if (userId != otherUserProfileAttribute.userId) {
      return false;
    }
    if (userProfileId != otherUserProfileAttribute.userProfileId) {
      return false;
    }
    */
    /*
    if ((label == null && otherUserProfileAttribute.label != null)
        || (label != null && !label.equals(otherUserProfileAttribute.label))) {
      return false;
    }
    */
    /*
    if (searchable != otherUserProfileAttribute.searchable) {
      return false;
    }
    */
	}
	
}
