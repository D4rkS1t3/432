package com.convert;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Frame {

    private static File selectedFile;
    private static File convertedFile;

    public static void createAndShowGUI() {
        // Tworzenie nowego obiektu JFrame
        JFrame frame = new JFrame("432Hz Converter");
        // Ustawienie domyślnej operacji zamknięcia okna na EXIT_ON_CLOSE
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Ustawienie układu ramki na siatkowy z jednym wierszem i pięcioma kolumnami
        frame.setLayout(new GridLayout(1, 5));

        // Tworzenie przycisku do otwierania okna wyboru pliku
        JButton openButton = new JButton("Open File");
        // Dodanie akcji do przycisku otwierającego
        openButton.addActionListener(new OpenButtonListener());

        // Tworzenie przycisku do wykonania konwersji
        JButton convertButton = new JButton("Convert");
        // Dodanie akcji do przycisku konwertującego
        convertButton.addActionListener(new ConvertButtonListener());

        // Tworzenie przycisku do zapisu przekonwertowanego pliku
        JButton saveButton = new JButton("Save");
        // Dodanie akcji do przycisku zapisującego
        saveButton.addActionListener(new SaveButtonListener());

        // Tworzenie przycisku do wyświetlenia pomocy
        JButton helpButton = new JButton("?");
        // Dodanie akcji do przycisku pomocy
        helpButton.addActionListener(new HelpButtonListener());

        // Dodanie przycisków do ramki
        frame.add(openButton);
        frame.add(convertButton);
        frame.add(saveButton);
        frame.add(helpButton);

        // Spakowanie elementów w ramce i ustawienie jej na widoczną
        frame.pack();
        frame.setVisible(true);
    }

    // Klasa obsługująca przycisk "Open File"
    private static class OpenButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Dodaj akcję otwierania okna wyboru pliku
            JFileChooser fileChooser=new JFileChooser();
            FileNameExtensionFilter filter=new FileNameExtensionFilter("Audio files", "mp3", "wav");
            fileChooser.setFileFilter(filter);
            int returnValue=fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                JOptionPane.showMessageDialog(null, "Wybrano plik: "+selectedFile.getName(), "Informacja", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // Klasa obsługująca przycisk "Convert"
    private static class ConvertButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Dodaj akcję wykonania konwersji
            convertedFile = convertTo432Hz(selectedFile);
        }
    }

    // Klasa obsługująca przycisk "Save"
    private static class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Sprawdzenie, czy istnieje przekonwertowany plik
            if (convertedFile != null) {
                // Tworzenie okna wyboru pliku
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save");
    
                // Ustawienie filtru plików na .wav
                FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV files", "wav");
                fileChooser.setFileFilter(filter);
    
                // Pokazanie okna dialogowego zapisu pliku
                int userSelection = fileChooser.showSaveDialog(null);
    
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    // Pobranie wybranego pliku
                    File fileToSave = fileChooser.getSelectedFile();
    
                    // Dodanie rozszerzenia .wav do nazwy pliku, jeśli go nie ma
                    if (!fileToSave.getAbsolutePath().endsWith(".wav")) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + ".wav");
                    }
    
                    // Przeniesienie przekonwertowanego pliku do wybranej lokalizacji
                    if (convertedFile.renameTo(fileToSave)) {
                        JOptionPane.showMessageDialog(null, "Plik został zapisany w lokalizacji: " + fileToSave.getAbsolutePath(), "Informacja", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Wystąpił błąd podczas zapisywania pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Nie ma pliku do zapisu", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    // Klasa obsługująca przycisk "Help"
    private static class HelpButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Dodaj akcję wyświetlenia informacji pomocy
            // Tu można dodać kod wyświetlający okno z pomocą
        }
    }

    // Metoda konwertująca plik dźwiękowy na 432Hz
    private static File convertTo432Hz(File file) {
        try {
            // Konwersja pliku MP3 na WAV
            String mp3File = file.getPath();
            String wavFile = "converted_file.wav";
            Converter converter = new Converter();
            converter.convert(mp3File, wavFile);
    
            // Wczytanie pliku WAV
            File wavFileConverted = new File(wavFile);
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(wavFileConverted);
            AudioFormat baseFormat = inputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                    false);
            AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, inputStream);
    
            JVMAudioInputStream stream = new JVMAudioInputStream(decodedInputStream);
            AudioDispatcher dispatcher = new AudioDispatcher(stream, 1024, 0);
    
            // Zmiana wysokości dźwięku o -0.318 (432 / 440)
            PitchShifter shifter = new PitchShifter(-0.318, decodedFormat.getSampleRate(), 1024, 0);
            dispatcher.addAudioProcessor(shifter);
    
            // Zapisanie wyniku do pliku
            File outputFile = new File("converted_file_432Hz.wav");
            AudioSystem.write(decodedInputStream, AudioFileFormat.Type.WAVE, outputFile);
    
            
            dispatcher.run();
    
            JOptionPane.showMessageDialog(null, "Plik został przekonwertowany na 432Hz", "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return outputFile;
        } catch (JavaLayerException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Wystąpił błąd podczas przetwarzania pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}