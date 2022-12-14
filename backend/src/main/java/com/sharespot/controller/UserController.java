package com.sharespot.controller;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespot.entity.Mail;
import com.sharespot.entity.User;
import com.sharespot.repo.BadgeRepository;
import com.sharespot.repo.CommentRepository;
import com.sharespot.repo.FollowRepository;
import com.sharespot.repo.GroupRepository;
import com.sharespot.repo.GroupUserRepository;
import com.sharespot.repo.MPRepository;
import com.sharespot.repo.MeetingRepository;
import com.sharespot.repo.PostLikeRepository;
import com.sharespot.repo.PostRepository;
import com.sharespot.repo.ScrapRepository;
import com.sharespot.repo.UserRepository;
import com.sharespot.service.JwtService;
import com.sharespot.service.JwtServiceImpl;
import com.sharespot.service.MailService;
import com.sharespot.service.UserService;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	@Autowired
	private JwtServiceImpl jwtService;

	@Autowired
	private UserService userService;
	private final UserRepository userRepository;
	
	//?????? ????????? ?????? repo, service ??????
	@Autowired
	private MPRepository mpRepository;   				//d
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private GroupRepository groupRepository; 			//d
	@Autowired
	private FollowRepository followRepository;			//d
	@Autowired
	private CommentRepository commentRepository;		//d
	@Autowired
	private ScrapRepository scrapRepository;			//d
	@Autowired
	private PostLikeRepository postLikeRepository;		//d
	@Autowired
	private BadgeRepository badgeRepository;			//d
	@Autowired
	private GroupUserRepository groupUserRepository;  	//d
	@Autowired
	private MeetingRepository meetingRepository; 		//d
	///////////////////////////////////////////////
	

	@Autowired
	private MailService mailService;

	@PostMapping("/signup") // ?????? ??????
	@ApiOperation(value = "?????? ??????")
	public ResponseEntity<Map<String, Object>> signUp(@RequestBody User user) throws ParseException {

		Map<String, Object> result = new HashMap<>();
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		User userEntity = User.builder().user_id(user.getUser_id()).email(user.getEmail()).password(user.getPassword())
				.phone(user.getPhone()).gender(user.getGender()).birth(user.getBirth()).name(user.getName())
				.nickname(user.getNickname()).introduce(user.getIntroduce()).profileImage(user.getProfileImage())
				.BD(user.getBD()) // isBadge
				.AL(user.getAL()) // isalarm
				.GD(user.getGD()) // isgender
				.BR(user.getBR()) // isBirth
				.PB(user.getPB()) // isPublic
				.userGrade(user.getUserGrade())
				.registerTime(sdf.format(timestamp)).build();

		try {
			User savedUser = userService.createUser(userEntity);
			String token = jwtService.create("user_id", user.getUser_id(), "Authorization");
			result.put("Authorization", token);
			result.put("message", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("message", FAIL);
		}
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}

	@GetMapping("/idcheck/{email}")
	@ApiOperation(value = "?????????(?????????) ?????? ??????")
	public ResponseEntity<Boolean> checkid(@PathVariable String email) {

		boolean check = userService.idCheck(email);

		return ResponseEntity.ok(userService.idCheck(email));

	}

	@Transactional
	@DeleteMapping("{id}")
	@ApiOperation(value = "?????? ??????")
	public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable int id, HttpServletRequest request) {

		Map<String, Object> result = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
		System.out.println("?????? ?????????");
		if (jwtService.isUsable(request.getHeader("Authorization"))) {
			if (jwtService.getUserId() == id) {
				// ????????? ????????? ?????? ?????? ?????? ????????????
				System.out.println(id);
				try {
					// ????????? ????????? ??????.
					
					//????????? ?????? ?????? ????????? ??????????????????
					
					User user = userService.getUser(id).get();
					mpRepository.deleteAllByUserId(user.getUser_id());
					
					List<Integer> gid = groupRepository.findAllGroupIdByGroupManager(user.getUser_id());
					
					for(Integer g : gid) {
						meetingRepository.deleteAllByGroupId(g);
					}
					groupUserRepository.deleteAllByUserId(user.getUser_id());
					groupRepository.deleteAllByGroupManager(user.getUser_id());
					
					commentRepository.deleteAllByUserId(user.getUser_id());
					scrapRepository.deleteAllByUserId(user.getUser_id());
					
					postLikeRepository.deleteAllByUserId(user.getUser_id());
					
					badgeRepository.deleteAllByUserId(user.getUser_id());
					
					followRepository.deleteAllByUserId(user.getUser_id());
					followRepository.deleteAllByFollowerId(user.getUser_id());
					
					postRepository.deleteAllByUserId(user.getUser_id());							
					//??? ?????? ?????? ?????? ??????
					userService.deleteUser(id);
					result.put("message", SUCCESS);
					result.put("Authorization", null);
					status = HttpStatus.ACCEPTED;
				} catch (Exception e) {
					logger.error("?????? ?????? ?????? ??????: {}", e);
					result.put("message", e.getMessage());
					status = HttpStatus.ACCEPTED;
				}
			} else {
				// ?????? ????????? ????????? ??? ??????
				result.put("message", FAIL);
			}

		} else {
			// ?????? ????????? ???????????? ??????
			result.put("Authorization", null);
			result.put("message", FAIL);
		}
		return new ResponseEntity<Map<String, Object>>(result, status);

	}

	@GetMapping("/logout")
	@ApiOperation(value = "?????? ????????????")
	public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) throws Exception {

		logger.debug("logout - ??????");
		Map<String, Object> result = new HashMap<>();

		if (jwtService.isUsable(request.getHeader("Authorization"))) {
			result.put("Authorization", null);
			result.put("message", SUCCESS);
		} else {
			result.put("message", FAIL);
		}

		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);

	}

	@GetMapping("/valid")
	@ApiOperation(value = "?????? ????????? ??????")
	public ResponseEntity<Map<String, Object>> tokenValidation(HttpServletRequest request) {
		logger.info("tokenValidation");
		Map<String, Object> result = new HashMap<>();
		if (jwtService.isUsable(request.getHeader("Authorization"))) {
			result.put("message", SUCCESS);
		} else {
			result.put("Authorization", null);
			result.put("message", FAIL);
		}
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}

	@GetMapping("/info/{userid}")
	@ApiOperation(value = "?????? ????????? ????????? ????????????", notes = "????????? ????????? ????????? ????????? ????????? ????????????")
	public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable("userid") int userid,
			@ApiParam(value = "????????? ????????? ?????????.", required = true) HttpServletRequest request) {
		// logger.debug("userid : {} ", userid);
		Map<String, Object> result = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
//		System.out.println(userid);
		if (jwtService.isUsable(request.getHeader("Authorization"))) {
			if (jwtService.getUserId() == userid) {
				// ????????? ????????? ?????? ?????? ?????? ????????????
				try {
					// ????????? ????????? ??????.
					Optional<User> userInfo = userService.getUser(userid);
					result.put("userInfo", userInfo);
					result.put("message", SUCCESS);
					status = HttpStatus.ACCEPTED;
				} catch (Exception e) {
					logger.error("???????????? ?????? : {}", e);
					result.put("message", e.getMessage());
					status = HttpStatus.ACCEPTED;
				}
			} else {
				// ?????? ????????? ????????? ??? ??????
				result.put("message", FAIL);
			}

		} else {
			// ?????? ????????? ???????????? ??????
			result.put("Authorization", null);
			result.put("message", FAIL);
		}
		return new ResponseEntity<Map<String, Object>>(result, status);
	}

	@GetMapping("info/user/{userId}")
	@ApiOperation(value = "?????? ????????? ??????????????? ??????", notes = "??????, ?????????, ?????????, ??????????????????, ??????????????????, ????????????, ????????????")
	public ResponseEntity<Object[]> getOtherUserInfo(@PathVariable("userId") int userId) {
		Object[] users = userRepository.findByUserId(userId);
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@PutMapping("{userid}")
	@ApiOperation(value = "?????? ??????", notes = "????????? ????????? ???????????? ??????????????? ????????????")
	public ResponseEntity<Map<String, Object>> modifyUser(@RequestBody User user, HttpServletRequest request)
			throws Exception {

		User userEntity = User.builder().user_id(user.getUser_id()).email(user.getEmail()).password(user.getPassword())
				.phone(user.getPhone()).gender(user.getGender()).birth(user.getBirth()).name(user.getName())
				.nickname(user.getNickname()).introduce(user.getIntroduce()).profileImage(user.getProfileImage())
				.BD(user.getBD()) // isBadge
				.AL(user.getAL()) // isalarm
				.GD(user.getGD()) // isgender
				.BR(user.getBR()) // isBirth
				.PB(user.getPB()) // isPublic
				.build();

		Map<String, Object> result = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;

		// ????????? ????????? ?????? ?????? ?????? ????????????
		try {
			// ????????? ????????? ??????.
			userService.modify(user.getEmail(), userEntity);
			result.put("message", SUCCESS);
			status = HttpStatus.ACCEPTED;
		} catch (Exception e) {
			logger.error("?????? ?????? ???????????? ??????: {}", e);
			result.put("message", e.getMessage());
			status = HttpStatus.ACCEPTED;
		}

		return new ResponseEntity<Map<String, Object>>(result, status);

	}

	@PostMapping("/login")
	@ApiOperation(value = "?????? ?????????", notes = "????????? ????????? ???????????? ?????????")
	public ResponseEntity<Map<String, Object>> login(
			@RequestBody @ApiParam(value = "???????????? ???????????? ??????????????? ??????.", required = true) User user) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		try {
			User loginUser = userService.login(user.getEmail(), user.getPassword());
			logger.debug("????????? ???????????? : {}", user.getEmail());
			if (loginUser != null) {
				String token = jwtService.create("userid", loginUser.getUser_id(), "Authorization");// key, data,
																									// subject
				logger.debug("????????? ???????????? : {}", token);
				resultMap.put("Authorization", token);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {

				resultMap.put("message", FAIL);
				status = HttpStatus.ACCEPTED;
			}
		} catch (Exception e) {
			System.out.println("==== catch ??????=====");
			logger.error("????????? ?????? : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	@PostMapping("/login/reset/{email}")
	@ApiOperation(value = "???????????? ?????????", notes = "?????? ???????????? ???????????? ?????????")
	public ResponseEntity<Integer> resetPassword(@PathVariable String email) {

		int result = 0;

		if (userService.idCheck(email)) {

			User user = userRepository.findByEmail(email).get();
			Mail mail = userService.sendEmailService(email, user.getName());

			mailService.mailSend(mail);
			result = 1;
		}

		return new ResponseEntity<Integer>(result, HttpStatus.OK);
	}
	
	@GetMapping("/list")
	@ApiOperation(value = "?????? ?????? ????????? ??????",notes = "????????? ?????? ????????? ????????????.")
	public ResponseEntity<List<User>> userList(){
		
		List<User> users = userService.getUsers();
		
		return new ResponseEntity<List<User>>(users,HttpStatus.OK);
	}
	
	@GetMapping("/grade/{userId}")
	@ApiOperation(value = "????????? ?????? ??????",notes = "????????? ????????? ????????? ?????? ????????? ????????????")
	public ResponseEntity<Integer> settingGrade(@PathVariable int userId){
		
		int result = 0;
		
		User user = userService.getUser(userId).get();
		
		if(user.getBD()==1) {
		
			result = userService.getMaxGrade(userId);		
		
			user.setUserGrade(result);
		
			userRepository.save(user);
			
			result = 1;
		}
		
		return new ResponseEntity<Integer>(result,HttpStatus.OK);
		
	}
	
	@PostMapping("/reset/password/{userId}")
	@ApiOperation(value = "?????? ???????????? ?????????", notes = "????????? ??????????????? ???????????????")
	public ResponseEntity<User> resetPassword(@PathVariable int userId, @RequestBody String pass){
		
		User temp = userService.resetPassword(pass, userId);
		
		return new ResponseEntity<User>(temp,HttpStatus.OK);
		
	}

}
