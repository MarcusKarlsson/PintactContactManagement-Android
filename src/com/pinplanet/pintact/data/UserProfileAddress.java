package com.pinplanet.pintact.data;


import java.io.Serializable;

/**
 * An address tied to a user profile record
 * 
 */
public class UserProfileAddress implements Serializable{

	/**
	 * Primary key for a profile
	 */
	private Long id;
	
	/**
	 * Link to parent profile record
	 */
	private long userProfileId;
	
	/**
	 * Link to parent user record
	 */
	private long userId;
	
	/**
	 * Label identifying the type of address (e.g., shipping, physical, etc.)
	 */
	private String label;
	
	/**
	 * True if this address lines can be used to search for this profile
	 */
	private boolean searchableAddress;
	
	/**
	 * True if this city and state can be used to search for this profile
	 */
	private boolean searchableCityState;
	
	/**
	 * Line 1 of an address
	 */
	private String addressLine1;
	
	/**
	 * Line 2 of an address
	 */
	private String addressLine2;
	
	/**
	 * City, town, or or village of an address
	 */
	private String city;
	
	/**
	 * State or providence of an address
	 */
	private String state;
	
	/**
	 * Country of an address
	 */
	private String country;
	
	/**
	 * Postal code of an address
	 */
	private String postalCode;

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
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isSearchableAddress() {
		return searchableAddress;
	}
	
	public void setSearchableAddress(boolean searchableAddress) {
		this.searchableAddress = searchableAddress;
	}
	
	public boolean isSearchableCityState() {
		return searchableCityState;
	}
	
	public void setSearchableCityState(boolean searchableCityState) {
		this.searchableCityState = searchableCityState;
	}
	
	public String getAddressLine1() {
		return addressLine1;
	}
	
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	
	public String getAddressLine2() {
		return addressLine2;
	}
	
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getPostalCode() {
		return postalCode;
	}
	
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	
	@Override
  public boolean equals(Object object) {
		
    UserProfileAddress otherUserProfileAddress = (UserProfileAddress)object;
		
    boolean result = super.equals(object);
    if (!result) {
      return false;
    }
    if (userId != otherUserProfileAddress.userId) {
      return false;
    }
    if (userProfileId != otherUserProfileAddress.userProfileId) {
      return false;
    }
    if ((label == null && otherUserProfileAddress.label != null)
        || (label != null && !label.equals(otherUserProfileAddress.label))) {
      return false;
    }
    if (searchableAddress != otherUserProfileAddress.searchableAddress) {
      return false;
    }
    if (searchableCityState != otherUserProfileAddress.searchableCityState) {
      return false;
    }
    if ((addressLine1 == null && otherUserProfileAddress.addressLine1 != null)
        || (addressLine1 != null && !addressLine1.equals(otherUserProfileAddress.addressLine1))) {
      return false;
    }
    if ((addressLine2 == null && otherUserProfileAddress.addressLine2 != null)
        || (addressLine2 != null && !addressLine2.equals(otherUserProfileAddress.addressLine2))) {
      return false;
    }
    if ((city == null && otherUserProfileAddress.city != null)
        || (city != null && !city.equals(otherUserProfileAddress.city))) {
      return false;
    }
    if ((state == null && otherUserProfileAddress.state != null)
        || (state != null && !state.equals(otherUserProfileAddress.state))) {
      return false;
    }
    if ((country == null && otherUserProfileAddress.country != null)
        || (country != null && !country.equals(otherUserProfileAddress.country))) {
      return false;
    }
    if (postalCode == null && otherUserProfileAddress.postalCode != null
        || (postalCode != null && !postalCode.equals(otherUserProfileAddress.postalCode))) {
      return false;
    }
    return true;
	}
	
}
