import java.io.*;
import java.lang.Thread;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;;
/**
 * Producer Consumer Copy File
 * java copyfile (src) (dst) (byte per iteration) (buffer size)
 * Copies a file from src to dst concurrently 
 * Needs code clean up, kinda dirty but works
 * Thread.sleep() is unecessary but left there just in case 
 *  -Elizabeth Ortiz
 */

class Producer implements Runnable{ //producer object 

	private FileInputStream reader; //reader object
	private String input_File;      //inputfile 
	private int max_Copy;			//bits per iteration
	private int count;              //counter
	private int x;                  //temp variable
	private byte data;              //holds what is being read
	private BlockingQueue<Byte> queue = null; //critical region buffer

	public Producer(BlockingQueue<Byte> queue, String input_File, int max_Copy){ //constructor 
		this.reader = null;            
		this.input_File = input_File;
		this.max_Copy = max_Copy;
		this.queue = queue;
	}

	public void run() {
		System.out.println("Producer Start"); //producer start

		try{

			reader = new FileInputStream(input_File);  //open file

			while( (x = reader.read()) != -1){ //read until end of file
											   //had to use x as a temp var because conversion into byte 
				data = (byte)x; 			   //had overflow when reading 0xffff producing -1 cutting loop short
				count = 0;                     //reset counter
				queue.put(data);			   //add to buffer
				while(count < max_Copy-1){     //get bytes
					if( (x = reader.read()) == -1 ){ //end of file check
						break; 
					}else{
						data = (byte)x;         //convert to byte
						queue.put(data);        // save
						System.out.println("Putting: "+data);
						count++;
					}
				}
				//Thread.sleep(1);    
			}
			System.out.println("Producer Done");
		}catch(FileNotFoundException e){               
			System.out.println("FILE NOT FOUND");
		}catch(IOException e){
			System.out.println("IOException");
		}catch(InterruptedException e){
			System.out.println("InterruptedException");
		}finally{
			try{
				reader.close();                              //close file
				System.out.println("Producer File Closed");
			}catch(IOException e){
				System.out.println("Closing Exception")/;
			}
		}	
	}
}

class Consumer implements Runnable{			   //consumer object
	FileOutputStream writer;                   //writer object
	private String output_File;
	private Byte out;
	private byte in;                         
	private BlockingQueue<Byte> queue = null;  //critical region buffer
	
	public Consumer(BlockingQueue<Byte> queue, String output_File){ //constructor
		this.writer = null;
		this.output_File = output_File;
		this.queue = queue;
	}

	public void run(){
		try{
			writer = new FileOutputStream(output_File);  //make new file

			System.out.println("Consumer Start");            //consumer start
			while(true){
				try{
					out = queue.poll(100,TimeUnit.MILLISECONDS);
					if(out == null){
						break;
					}else{
						in = out;
						writer.write(out);
					                                       //take from queue and write to file
						System.out.println("Taking");
					}
				}catch(IOException e){
					System.out.println("IOException");       
				}
			}
		}catch(FileNotFoundException e){
			System.out.println("FileNotFoundException");
		}catch(InterruptedException e){
			System.out.println("InterruptedException");
		}finally{
			try{
				writer.close();                               //close file
				System.out.println("Copy Finished");
			}catch(IOException e){
				System.out.println("IOException");
			}
		}
	}
}

public class copyfile{
	public static void main(String [] args){
		
		int buffer_Size = Integer.parseInt(args[3]);  //get buffer size

		int max_Copy = Integer.parseInt(args[2]);     //get bytes per iteration

		BlockingQueue<Byte> queue = new ArrayBlockingQueue(buffer_Size); //critical region

		Producer producer = new Producer(queue,args[0],max_Copy);  //producer object
		Consumer consumer = new Consumer(queue,args[1]);           //consumer object
		new Thread(producer).start();                        //RUN
		new Thread(consumer).start();
	}
}
