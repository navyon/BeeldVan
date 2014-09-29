package com.ngagemedia.beeldvan.model;

/**
 * Created by admin on 9/7/2014.
 */
public class Message
	{
		private long timestamp;
		private String msg;
		String ip_address;
		private String email;
		private String foto;
		int locationid;

		public Message( String msg, String ip, String email, String foto, int locationID )
			{

				this.email = email;
				this.foto = foto;
				this.ip_address = ip;
				this.msg = msg;
				this.locationid = locationID;
			}

		public String getEmail()
			{
				return email;
			}

		public String getFoto()
			{
				return foto;
			}

		public String getIp_address()
			{
				return ip_address;
			}

		public String getMsg()
			{
				return msg;
			}

		public int getLocationid()
			{
				return locationid;
			}
	}
