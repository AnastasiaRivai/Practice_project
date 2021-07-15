package com.train.parsing_site_trains;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;

public class App {

	private static Document getPage() throws IOException {
		String url = "https://poezdato.net/raspisanie-po-stancyi/samara/elektrichki/";
		Document page = Jsoup.parse(new URL(url), 3000);
		
		return page;
	}
	
	private static String[] getNumbers(Elements allLinks) {
		String hrefs[] = new String[allLinks.size()];
		
		for (int i = 0; i < allLinks.size(); i++) {
			hrefs[i] = allLinks.get(i).attr("href");
		}
		
		String numbers[] = new String[allLinks.size()]; // Номера поездов
		int index[] = new int[allLinks.size()];
		
		for (int i = 0; i < index.length; i++) {
			index[i] = 0;
		}
		
		for (int i = 0; i < allLinks.size(); i++) {
			index[i] = hrefs[i].indexOf("raspisanie-elektrichki");
		}
		
		for (int i = 0; i < index.length; i++) {
			if (index[i] != -1)
				numbers[i] = allLinks.get(i).text();
		}
		
		String tmp[] = new String[allLinks.size()];
		
		int i = 0;
		int j = 0;
		
		while (i < index.length) {
			if (numbers[i] != null) {
				tmp[j] = numbers[i];
				j++;
			}
			i++;
		}
		
		return tmp;
	}
	
	private static String[] getRouts(Elements allLinks) {
		
		String hrefs[] = new String[allLinks.size()];
		String routs[] = new String[allLinks.size()];
		
		for (int i = 0; i < allLinks.size(); i++) {
			hrefs[i] = allLinks.get(i).attr("href");
		}
		
		int index[] = new int[allLinks.size()];
		
		for (int i = 0; i < index.length; i++) {
			index[i] = 0;
		}
		
		for (int i = 0; i < allLinks.size(); i++) {
			index[i] = hrefs[i].indexOf("raspisanie-po-stancyi");
		}
		
		for (int i = 0; i < index.length; i++) {
			if (index[i] != -1) {
				routs[i] = allLinks.get(i).text();
			}
		}		
		
		String tmp[] = new String[allLinks.size()];
		
		int i = 0;
		int j = 0;
		
		while (i < index.length) {
			if (routs[i] != null) {
				tmp[j] = routs[i];
				j++;
			}
			i++;
		}
		
		return tmp;
	}

	
	public static void main(String[] args) throws IOException {
		Document page = getPage();
		
		// Находим всю таблицу расписания
		Element tableRasp = page.select("table[ class=schedule_table stacktable desktop]").first();
		
		
		if (tableRasp == null) {
			System.out.println("Нет таблицы!");
			System.exit(1);
		}
		
		// на сайте номера и маршруты хранятся в ссылках, сначала найдём все ссылки
		
		Elements allLinks = tableRasp.select("td > a");
		
		Elements allSpan = tableRasp.select("span[class=_time]"); // Каждый чётный (начиная с нулевого) такой span - прибытие, каждый нечётный - отправление.
		
		String arrives[] = new String[allSpan.size()];
		String departure[] = new String[allSpan.size()];
		
		for (int i = 0; i < allSpan.size(); i++) {
			if (i % 2 == 0)
				arrives[i] = allSpan.get(i).text();
			else 
				departure[i] = allSpan.get(i).text();
		}
		
		String numbers[] = getNumbers(allLinks);
		String routs[] = getRouts(allLinks);
		
		// Вывод результатов парсинга в консоль (без работы с БД)
		/*for (int i = 0, j = 0, k = 0, l = 1; i < numbers.length && j < routs.length && k < arrives.length && l < departure.length; i++, j = j + 2, k = k + 2, l = l + 2) {
			if (numbers[i] != null) {
				System.out.print(numbers[i] + " ");
			}
			if (routs[j] != null && routs[j + 1] != null) {
				System.out.print(routs[j] + " - " + routs[j + 1] + " ");
			}
			if (arrives[k] != null) {
				System.out.print(arrives[k] + " - ");
			}
			if (departure[l] != null) {
				System.out.println(departure[l]);
			}
		}*/
		
		// Подключение к БД
		try {
			String url = "jdbc:mysql://localhost:3306/train_routes";
			String username = "root";
			String password = "!tribunal!2004mustDIE";
			
			Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance(); // путь к файлу пакету драйвера БД
			
			try (Connection conn = DriverManager.getConnection(url, username, password)){
				System.out.println("Соединение с БД успешно создано!");
				
				String sql = "INSERT INTO route(Number_route, Arrival_station, Departure_station) Value (?, ?, ?)";
				
				Statement statement = conn.createStatement();
				
				PreparedStatement pStatement = conn.prepareStatement(sql);
				
				// Если БД не пуста - удаляем всё перед записью
				statement.executeUpdate("DELETE FROM route");	
				
				// Запись в БД
				for (int i = 0, j = 0; i < 17 && j < routs.length; i++, j = j + 2) {
					if (numbers[i] != null) {
						pStatement.setString(1, numbers[i]);
					}
					if (routs[j] != null && routs[j + 1] != null) {
						pStatement.setString(2, routs[j]);
						pStatement.setString(3, routs[j + 1]);
					}
					pStatement.executeUpdate();
				}
				
				// Вывод из БД в консоль
				ResultSet result = statement.executeQuery("SELECT * FROM route");
				while (result.next()) {
					int id = result.getInt("ID_route");
					String number = result.getString("Number_route");
					String arrival = result.getString("Arrival_station");
					String departuret = result.getString("Departure_station");
					
					System.out.println(id + " " + number + " " + arrival + " - " + departuret);
				}
				
				System.out.println("Работа с БД успешно закончена!");
			}		
		}
		catch(Exception ex) {
			System.out.println("Соединение аварийно прервано!");
		}
		
		
	}

}
