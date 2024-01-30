import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
class DownloadTask implements Runnable {
private final String fileURL;
private final String saveDir;
private final String fileName;
private final File file;
private HttpURLConnection httpConn;
private boolean isPaused;
public DownloadTask(String fileURL, String saveDir, String fileName) {
this.fileURL = fileURL;
this.saveDir = saveDir;
this.fileName = fileName;
this.file = new File(saveDir, fileName);
this.isPaused = false;
}
public void pauseDownload() {
isPaused = true;
httpConn.disconnect();
}
public void resumeDownload() {
isPaused = false;
run();
}
public void cancelDownload() {
if (httpConn != null) {
httpConn.disconnect();
}
if (file.exists()) {
file.delete();
}
}
public boolean isPaused() {
return isPaused;
}
public boolean isCompleted() {

return file.exists() && file.length() > 0;
}
public String getFileName() {
return fileName;
}
@Override
public void run() {
try {
URL url = new URL(fileURL);
httpConn = (HttpURLConnection) url.openConnection();
if (file.exists()) {
long existingFileSize = file.length();
httpConn.setRequestProperty("Range", "bytes=" + existingFileSize + "-");
}
InputStream inputStream = httpConn.getInputStream();
RandomAccessFile outputStream = new RandomAccessFile(file, "rw");
outputStream.seek(file.length());
int bytesRead;
byte[] buffer = new byte[1024];
while (!isPaused && (bytesRead = inputStream.read(buffer)) != -1) {
outputStream.write(buffer, 0, bytesRead);
}
outputStream.close();
inputStream.close();
httpConn.disconnect();
if (!isPaused) {
System.out.println("Download of '" + fileName + "' completed.");
} else {
System.out.println("Download of '" + fileName + "' paused.");
}
} catch (IOException e) {
e.printStackTrace();
}
}
}
public class ConsoleDownloadManager {
private static final int MAX_CONCURRENT_DOWNLOADS = 3;
private static final Scanner scanner = new Scanner(System.in);
private static final List<DownloadTask> downloads = new ArrayList<>();

private static final ExecutorService executor =
Executors.newFixedThreadPool(MAX_CONCURRENT_DOWNLOADS);
public static void main(String[] args) {
while (true) {
System.out.println("1. Download a New File");
System.out.println("2. Show Ongoing/Paused Downloads");
System.out.println("3. Pause a Download");
System.out.println("4. Resume a Download");
System.out.println("5. Cancel a Download");
System.out.println("6. Get Detailed Info of a Download");
System.out.println("7. Exit");
System.out.print("Choose an option: ");
int choice = scanner.nextInt();
scanner.nextLine(); // Consume the newline character
switch (choice) {
case 1:
downloadNewFile();
break;
case 2:
showOngoingDownloads();
break;
case 3:
pauseDownload();
break;
case 4:
resumeDownload();
break;
case 5:
cancelDownload();
break;
case 6:
getDownloadDetails();
break;
case 7:
executor.shutdownNow();
System.out.println("Exiting the program.");
System.exit(0);
break;
default:
System.out.println("Invalid choice. Please enter a valid option.");
}
}
}

private static void downloadNewFile() {
System.out.print("Enter the download link: ");
String fileURL = scanner.nextLine();
System.out.print("Enter the save directory: ");
String saveDir = scanner.nextLine();
System.out.print("Enter the file name: ");
String fileName = scanner.nextLine();
DownloadTask downloadTask = new DownloadTask(fileURL, saveDir, fileName);
downloads.add(downloadTask);
executor.submit(downloadTask);
System.out.println("Download of '" + fileName + "' started.");
}
private static void showOngoingDownloads() {
if (downloads.isEmpty()) {
System.out.println("No ongoing or paused downloads.");
return;
}
System.out.println("Ongoing/Paused Downloads:");
for (int i = 0; i < downloads.size(); i++) {
DownloadTask download = downloads.get(i);
System.out.println((i + 1) + ". " + download.getFileName() +
(download.isPaused() ? " (Paused)" : (download.isCompleted() ? " (Completed)" : "(Downloading)")));
}
}
private static void pauseDownload() {
showOngoingDownloads();
System.out.print("Enter the download number to pause: ");
int downloadNumber = scanner.nextInt();
scanner.nextLine(); // Consume the newline character
if (downloadNumber > 0 && downloadNumber <= downloads.size()) {
DownloadTask download = downloads.get(downloadNumber - 1);
if (!download.isCompleted() && !download.isPaused()) {
download.pauseDownload();
System.out.println("Download paused: " + download.getFileName());
} else {
System.out.println("Cannot pause this download.");
}
} else {
System.out.println("Invalid download number.");
}

}
private static void resumeDownload() {
showOngoingDownloads();
System.out.print("Enter the download number to resume: ");
int downloadNumber = scanner.nextInt();
scanner.nextLine(); // Consume the newline character
if (downloadNumber > 0 && downloadNumber <= downloads.size()) {
DownloadTask download = downloads.get(downloadNumber - 1);
if (!download.isCompleted() && download.isPaused()) {
download.resumeDownload();
System.out.println("Download resumed: " + download.getFileName());
} 
else {
System.out.println("Cannot resume this download.");
}
} 
else {
System.out.println("Invalid download number.");
}
}
private static void cancelDownload() {
showOngoingDownloads();
System.out.print("Enter the download number to cancel: ");
int downloadNumber = scanner.nextInt();
scanner.nextLine(); // Consume the newline character
if (downloadNumber > 0 && downloadNumber <= downloads.size()) {
DownloadTask download = downloads.get(downloadNumber - 1);
download.cancelDownload();
downloads.remove(download);
System.out.println("Download canceled: " + download.getFileName());
} 
else {
System.out.println("Invalid download number.");
}
}
private static void getDownloadDetails() {
showOngoingDownloads();
System.out.print("Enter the download number to get details: ");
int downloadNumber = scanner.nextInt();
scanner.nextLine(); // Consume the newline character
if (downloadNumber > 0 && downloadNumber <= downloads.size()) {
DownloadTask download = downloads.get(downloadNumber - 1);

System.out.println("Download Details for " + download.getFileName() + ":");
System.out.println("Paused: " + download.isPaused());
System.out.println("Completed: " + download.isCompleted());
} else {
System.out.println("Invalid download number.");
}
}
}