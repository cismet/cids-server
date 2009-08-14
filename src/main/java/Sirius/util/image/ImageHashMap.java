package Sirius.util.image;

import java.util.*;
import java.io.*;
import javax.swing.*;

public class ImageHashMap extends HashMap implements java.io.Serializable
{
	public ImageHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public ImageHashMap(String[] names, String[] files)
	{
		super(names.length, 1.0f);

		if (names.length == files.length)
		{
			for (int i = 0; i < names.length; i++)
				this.put(names[i], files[i]);
		}
	}

	public ImageHashMap(Image[] images)
	{
		super(images.length, 1.0f);
		for (int i = 0; i < images.length; i++)
			this.put(images[i]);
	}



	public ImageIcon put(String name, String file)
	{
		ImageIcon icon = new ImageIcon(new Image(file).getImageData(), name);
		super.put(name, icon);
		return this.get(name);
	}

	public ImageIcon put(Image image)
	{
		ImageIcon icon = new ImageIcon(image.getImageData(), image.getName());
		super.put(image.getName(), icon);
		return this.get(image.getName());
	}

	public ImageIcon put(File file)
	{
			ImageIcon icon = new ImageIcon(new Image(file).getImageData(), file.getName().trim());
			super.put(file.getName().trim(), icon);
			return this.get(file.getName().trim());
	}
	public ImageIcon get(String name)

	{
		return (ImageIcon)super.get(name);
	}
}
