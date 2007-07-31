package environmentShell;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import rlglue.environment.Environment;

public class EnvShellDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String curDir = System.getProperty("user.dir");
		File d= new File(curDir);
		String workSpaceDir=d.getParent();

		Vector<File> theFiles=new Vector<File>();

		String envJarDirString=workSpaceDir+"/envJars/";
		File envJarDir=new File(envJarDirString);
		File [] theFileList=envJarDir.listFiles();
		for (File thisFile : theFileList) {
			if(thisFile.getName().endsWith(".jar"))
				theFiles.add(thisFile);
		}

		System.out.println("Found: "+theFiles.size()+ " jar files");

		File theFile=theFiles.get(0);
		String theFileName=theFile.getAbsolutePath();
		System.out.println("I think the name is: "+theFileName);
		URLClassLoader urlLoader = null;
		JarInputStream jis=null;
		JarEntry entry=null;
		int loadedCount = 0, totalCount = 0;

		try {
			urlLoader = getURLClassLoader(new URL("file", null, theFileName));

			jis = new JarInputStream(new FileInputStream(theFileName));

			entry = jis.getNextJarEntry();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		while (entry != null) {
			String name = entry.getName();
			if (name.endsWith(".class")) {
				totalCount++;
				name = name.substring(0, name.length() - 6);
				name = name.replace('/', '.');
				System.out.print("> " + name);
				

				try {
					Class theEnvClass=urlLoader.loadClass(name);
					Method []theMethods=theEnvClass.getMethods();
					for (Method thisMethod : theMethods) {
						System.out.println(thisMethod.getName());
					}
					System.out.println("\t- loaded");
					//Lets try and instantiate it:
					try {
						Environment theEnv=(Environment)theEnvClass.newInstance();
						System.out.println("We have an Env!");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} catch (Throwable e) {

				}

			}
			try {
				entry = jis.getNextJarEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}





	private static URLClassLoader getURLClassLoader(URL jarURL) {
		return new URLClassLoader(new URL[]{jarURL});
	}

}
