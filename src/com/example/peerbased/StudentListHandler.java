package com.example.peerbased;

import java.util.ArrayList;

public class StudentListHandler {
	private static ArrayList<Student> studentsList;
	public static ArrayList<Student> getList()
	{
		return studentsList;
	}
	public static void setList(ArrayList<Student> s)
	{
		studentsList = s;
	}
	static
	{
		studentsList = new ArrayList<Student>();
	}
}
