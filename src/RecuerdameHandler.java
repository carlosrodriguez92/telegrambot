import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RecuerdameHandler extends TelegramLongPollingBot {
	
	Timer recuerdameTimer = new Timer();
	
	@Override
	public String getBotUsername() {
		// TODO Auto-generated method stub
		return BotConfig.RECUERDAME_USERNAME;
	}
	
	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return BotConfig.RECUERDAME_TOKEN;
	}


	@Override
	public void onUpdateReceived(Update update) {
		//comprobamos si el update contiene un mensaje
		if (update.hasMessage()){
			recuerdameHandler(update);
		}

	}
	
	public void sendRecuerdame(SendMessage sendMessageRecuerdame){		
		try{
			sendMessage(sendMessageRecuerdame);
		}catch(TelegramApiException e){
			e.getMessage();
		}

	}
	
	public Date generarMomentoDeRecordar(String tiempo){
		Calendar horaActual = Calendar.getInstance();
		Calendar momentoDeRecordar = horaActual;
		int minutos;
		try{
			minutos = Integer.parseInt(tiempo);
		}catch(NumberFormatException e){
			return null;
		}
		
		momentoDeRecordar.add(Calendar.MINUTE, minutos);
		Date dateRecuerdame = momentoDeRecordar.getTime();
		return dateRecuerdame;		
	}
	
	
	//Solo funciona con minutos (provisional)
	public String[] separarTiempoYMensaje(Message message){
		String rawText = message.getText();
		String tiempo = null;
		String mensaje = null;
		if(rawText.contains(" ")){
			String text = rawText.substring(rawText.indexOf(" "));
			if (text.contains("m")){
				tiempo = text.substring(text.indexOf(" ")+1, text.indexOf('m'));
				String mensajeRaw = text.substring(text.indexOf('m'));
				if(mensajeRaw.contains(" ")){
					mensaje = mensajeRaw.substring(mensajeRaw.indexOf(" "));
					String[] infoRecuerdame = {tiempo, mensaje};		
					return infoRecuerdame;
				}else{
					return null;
				}
				
			}else{
				return null;
			}	
		}else{
			return null;
		}
					
	}
	
	public void recuerdameHandler(Update update){
		//Preparamos el mensaje a enviar, la hora actual y el formato de la hora
		Message message = update.getMessage();		
		DateFormat dateFormat = new SimpleDateFormat("d 'de' MMMMM 'a las' HH:mm");
//		Calendar calendarActual = Calendar.getInstance();
//		Date dateActual = calendarActual.getTime();
		
		//comprobamos si el mensaje es de tipo texto	
		if(message.hasText()){
			SendMessage sendMessageRequest = new SendMessage();
			sendMessageRequest.setChatId(message.getChatId().toString());

			/*sendMessageRequest.setText("Has dicho: "+message.getText()+ " el: "+dateFormat.format(dateActual));
			try{
				sendMessage(sendMessageRequest);
			}catch(TelegramApiException e){
				e.getMessage();
			}			
			*/
			if(message.getText().startsWith("/start")){
				sendMessageRequest.setText("¡Hola! Para usarme escribe /recuerdame");
				try{
					sendMessage(sendMessageRequest);
				}catch(TelegramApiException e){
					e.getMessage();
				}
			}
			//comprobamos si el mensaje es un comando /alert
			if(message.getText().startsWith("/recuerdame")){
				String[] infoRecuerdame = separarTiempoYMensaje(message);
				if (infoRecuerdame != null){
					System.out.println(infoRecuerdame[0]+" "+infoRecuerdame[1]);
					Date momentoDeRecordar = generarMomentoDeRecordar(infoRecuerdame[0]);
					if(momentoDeRecordar != null){
						//Se genera un TimerTask con la informacion necesaria para enviar el mensaje a recordar mas tarde
						SendMessage sendMessageRecuerdame = new SendMessage();
						sendMessageRecuerdame.setText("RECUERDA: "+infoRecuerdame[1]);
						sendMessageRecuerdame.setChatId(message.getChatId().toString());
						recuerdameTimer.schedule(new TimerTask(){
							@Override
							public void run(){
								
								sendRecuerdame(sendMessageRecuerdame);
							}
						}, momentoDeRecordar);
						sendMessageRequest.setText("Vale, te lo recordaré el "+dateFormat.format(momentoDeRecordar));
					}else{
						sendMessageRequest.setText("Debes usar el formato: tiempo mensaje \n"
								+ "Por ejemplo: /recuerdame 10m sacar pizza del horno \n"
								+ "Por ahora solo funciona con minutos");
					}
				
				}else{
					sendMessageRequest.setText("Debes usar el formato: tiempo mensaje \n"
							+ "Por ejemplo: /recuerdame 10m sacar pizza del horno \n"
							+ "Por ahora solo funciona con minutos");
				}
				try{
					sendMessage(sendMessageRequest);
				}catch(TelegramApiException e){
					e.getMessage();
				}
			}				
		}
	}
	


	public static void main(String[] args) {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		try{
			telegramBotsApi.registerBot(new RecuerdameHandler());
		}catch(TelegramApiException e){
			e.getMessage();
		}

	}

}
