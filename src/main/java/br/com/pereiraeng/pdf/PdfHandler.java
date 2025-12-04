package br.com.pereiraeng.pdf;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfHandler {

	// extrair texto

	/**
	 * Função que, a partir da referência de um arquivo no formato pdf (Portable
	 * Document File) retorna a sequência dos textos nele contido
	 * 
	 * @param file objeto <code>File</code> com a referência do arquivo
	 * @return <code>String</code> com todos os textos contidos no arquivo
	 */
	public static String getPDFTextContent(File file) {
		String content = null;

		try {
			PDDocument document = PDDocument.load(file);
			PDFTextStripper s = new PDFTextStripper();
			content = s.getText(document);
			document.close();
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return content;
	}

	// extrair images

	/**
	 * Função que retorna as imagens de um arquivo PDF
	 * 
	 * @param file arquivo PDF
	 * @return lista de imagens
	 */
	public static List<RenderedImage> getImagesFromPDF(File file) {
		List<RenderedImage> images = new LinkedList<>();
		try {
			PDDocument document = PDDocument.load(file);
			for (PDPage page : document.getPages())
				images.addAll(getImagesFromResources(page.getResources()));
			document.close();
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return images;
	}

	private static List<RenderedImage> getImagesFromResources(PDResources resources) throws IOException {
		List<RenderedImage> images = new LinkedList<>();

		for (COSName xObjectName : resources.getXObjectNames()) {
			PDXObject xObject = resources.getXObject(xObjectName);

			if (xObject instanceof PDFormXObject)
				images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
			else if (xObject instanceof PDImageXObject)
				images.add(((PDImageXObject) xObject).getImage());
		}

		return images;
	}

	/**
	 * Função que extrai todos as imagens de um arquivo PDF e cria arquivos de
	 * imagem
	 * 
	 * @param pdfFile    arquivo PDF
	 * @param destFolder diretório destino para as imagens
	 * @param format     formato das imagens a serem criadas
	 */
	public static void extractImagesFromPDF(File pdfFile, File destFolder, String format) {
		int count = 0;
		try {
			PDDocument document = PDDocument.load(pdfFile);
			for (PDPage page : document.getPages())
				count = extractImagesFromPDF(page.getResources(), destFolder, format, count);
			document.close();
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	private static int extractImagesFromPDF(PDResources resources, File destFolder, String format, int count)
			throws IOException {
		for (COSName xObjectName : resources.getXObjectNames()) {
			PDXObject xObject = resources.getXObject(xObjectName);

			if (xObject instanceof PDFormXObject)
				count = extractImagesFromPDF(((PDFormXObject) xObject).getResources(), destFolder, format, count);
			else if (xObject instanceof PDImageXObject)
				ImageIO.write(((PDImageXObject) xObject).getImage(), format,
						new File(String.format("%s\\%03d.%s", destFolder.getAbsolutePath(), count++, format)));
		}

		return count;
	}
}