package com.moabam.support.fixture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.mock.web.MockMultipartFile;

import com.moabam.api.dto.member.ModifyMemberRequest;

public class ModifyImageFixture {

	public static MockMultipartFile makeMultipartFile() {
		try {
			File file = new File("src/test/resources/image.png");
			FileInputStream fileInputStream = new FileInputStream(file);

			return new MockMultipartFile("1", "image.png", "image/png", fileInputStream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ModifyMemberRequest modifyMemberRequest() {
		return new ModifyMemberRequest("intro", "sldsldsld");
	}

}
