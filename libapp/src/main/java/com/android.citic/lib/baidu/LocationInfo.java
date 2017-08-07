package com.android.citic.lib.baidu;

/**
 * 定位信息
 */
public class LocationInfo {

	private String time; 		// 定位时间
	private int errCode;		// 错误码
	private double latitude; 	// 纬度
	private double longitude; 	// 经度
	private float radius; 		// 半径
	private String address; 	// 位置信息
	private String province;    //省份
	private String city;		//城市
	private String cityCode;	//城市码

	public LocationInfo(Builder builder) {
		time = builder.time;
		errCode = builder.errCode;
		latitude = builder.latitude;
		longitude = builder.longitude;
		radius = builder.radius;
		address = builder.address;
		province = builder.province;
		city = builder.city;
		cityCode = builder.cityCode;
	}

	public String getTime() {
		return time;
	}

	public int getErrCode() {
		return errCode;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public float getRadius() {
		return radius;
	}

	public String getAddress() {
		return address;
	}

	public String getProvince() {
		return province;
	}

	public String getCity() {
		return city;
	}

	public String getCityCode() {
		return cityCode;
	}

	public static class Builder {
		private String time; 		// 定位时间
		private int errCode; 		// 错误码
		private double latitude;	// 纬度
		private double longitude; 	// 经度
		private float radius; 		// 半径
		private String address; 	// 位置信息
		private String province;	// 省份
		private String city;		// 城市
		private String cityCode;	//城市码

		public LocationInfo build() {
			return new LocationInfo(this);
		}

		public Builder setTime(String time) {
			this.time = time;

			return this;
		}

		public Builder setErrCode(int errCode) {
			this.errCode = errCode;

			return this;
		}

		public Builder setLatitude(double latitude) {
			this.latitude = latitude;

			return this;
		}

		public Builder setLongitude(double longitude) {
			this.longitude = longitude;

			return this;
		}

		public Builder setRadius(float radius) {
			this.radius = radius;

			return this;
		}

		public Builder setAddress(String address) {
			this.address = address;

			return this;
		}
		
		public Builder setProvince(String province){
			this.province = province;
			
			return this;
		}
		
		public Builder setCity(String city){
			this.city = city;
			
			return this;
		}
		
		public Builder setCityCode(String cityCode){
			this.cityCode = cityCode;
			return this;
		}
	}
}
