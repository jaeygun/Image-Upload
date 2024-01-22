package com.study.imageupload.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/file")
@Slf4j
public class FileController {

	// 파일 업로드할 디렉토리 경로
	private final String uploadDir = Paths.get("C:", "image").toString();

	@ResponseBody
	@PostMapping("/imageUpload")
	public Map<String, Object> imageUpload(@RequestParam MultipartFile image) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", "false");

		if (image.isEmpty()) {
			resultMap.put("msg", "이미지가 정상적으로 업로드되지 않았습니다.");
			log.info("이미지가 정상적으로 전달되지 않았습니다.");
			return resultMap;
		}

		// 파일 저장 디렉토리가 존재하지 않을 경우 디렉토리 생성
		File dir = new File(uploadDir);
		if (!dir.exists()) {
			dir.mkdir();
		}

		// 원본 파일명
		String orgFilename = image.getOriginalFilename();

		// 32자리 랜덤 문자열
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");

		// 확장자
		String extension = orgFilename.substring(orgFilename.lastIndexOf(".") + 1);

		// 디스크에 저장할 파일명
		String saveFilename = uuid + "." + extension;

		// 파일명을 포함한 파일이 저장되는 절대경로
		String fileFullPath = Paths.get(uploadDir, saveFilename).toString();

		try {
			File uploadFile = new File(fileFullPath);
			image.transferTo(uploadFile);
			resultMap.put("fileFullPath", fileFullPath);
			resultMap.put("result", "success");
			log.info("이미지가 저장 성공 > Path : [" + fileFullPath + "]");
		} catch (Exception e) {
			log.error("이미지 저장 중 에러가 발생했습니다.", e);
			resultMap.put("msg", "이미지 저장 중 에러가 발생했습니다.");
		}

		return resultMap;
	}

	/**
     * 디스크에 업로드된 파일을 byte[]로 반환
     * @param filename 디스크에 업로드된 파일명
     * @return image byte array
     */
	@ResponseBody
	@GetMapping(value = "/imagePrint", produces = { MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
	public byte[] printEditorImage(@RequestParam String fileName) {

		// base64로 인코딩된 파일명을 디코딩
		byte[] decodedBytes = Base64.getDecoder().decode(fileName);
		fileName = new String(decodedBytes);

		// 파일이 없는 경우 예외 throw
		File uploadedFile = new File(fileName);
		if (uploadedFile.exists() == false) {
			throw new RuntimeException();
		}

		try {
			// 이미지 파일을 byte[]로 변환 후 반환
			byte[] imageBytes = Files.readAllBytes(uploadedFile.toPath());
			return imageBytes;

		} catch (IOException e) {
			// 예외 처리는 따로 해주는 게 좋습니다.
			throw new RuntimeException(e);
		}
	}
	
}
