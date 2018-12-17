/*
 * Copyright (c) 2018 Marco Maccaferri and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.maccasoft.tools.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class ImageRegistry {

    private static final Map<Object, Image> map = new HashMap<Object, Image>();

    ImageRegistry() {
    }

    public static Image getImage(Object key) {
        return map.get(key);
    }

    public static void setImage(Object key, Image image) {
        Image oldImage = map.get(key);
        map.put(key, image);
        if (oldImage != null) {
            oldImage.dispose();
        }
    }

    public static void removeImage(Object key) {
        Image oldImage = map.get(key);
        map.remove(key);
        if (oldImage != null) {
            oldImage.dispose();
        }
    }

    public static void dispose() {
        for (Image image : map.values()) {
            image.dispose();
        }
        map.clear();
    }

    public static Image getImageFromResources(String name) {
        return getImageFromResources(ImageRegistry.class, name);
    }

    @SuppressWarnings("rawtypes")
    public static Image getImageFromResources(Class clazz, String name) {
        if (map.get(name) == null) {
            InputStream is = clazz.getResourceAsStream(name);
            ImageLoader loader = new ImageLoader();
            ImageData[] data = loader.load(is);
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.put(name, new Image(Display.getDefault(), data[0]));
        }
        return map.get(name);
    }
}
