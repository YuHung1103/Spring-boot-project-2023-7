package com.example.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Dao.BlacklistRepository;
import com.example.demo.Dao.UserRepository;
import com.example.demo.Entity.Blacklist;
import com.example.demo.Entity.User;
import com.example.demo.Jwt.JwtTokenGenerator;

@Service
public class UserService {
	
	private final UserRepository userRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final BlacklistRepository blacklistRepository;

    public UserService(UserRepository userRepository, JwtTokenGenerator jwtTokenGenerator, BlacklistRepository blacklistRepository) {
        this.userRepository = userRepository;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.blacklistRepository = blacklistRepository;
    }
	
    //取得所有user資料(對userRepository操作)
	public List<User> getAllUsers(){
		return userRepository.findAll();
	}
	
	//取得指定user資料(對userRepository操作)
	public User getUser(int Id){
		return userRepository.findById(Id)
				.orElseThrow(() -> new RuntimeException("Not Found"));
	}
	
	//新增資料(對userRepository操作)
	public User createUser(User newUser) {
		return userRepository.save(newUser);
	}
	
	//更新資料(對userRepository操作) 先找有沒有此ID，再把新的資料set進舊的資料
	public User updateUser(int Id, User updatedUser) {
		User user = userRepository.findById(Id)
				.orElseThrow(() -> new RuntimeException("Not Found"));
		user.setUserAccount(updatedUser.getUserAccount());
		user.setUserPassword(updatedUser.getUserPassword());
		user.setUserName(updatedUser.getUserName());
		user.setUserPhone(updatedUser.getUserPhone());
		user.setUserEmail(updatedUser.getUserEmail());
		user.setRole(updatedUser.getRole());
		return userRepository.save(user);
	}
	
	//刪除資料(對userRepository操作) 先找有沒有此ID，然後再做刪除
	public String deleteUser(int Id) {
		boolean exists = userRepository.existsById(Id);
		if(!exists) {
			return "Not Found";
		}
		userRepository.deleteById(Id);
		return "Success";
	}
	
	//登入帳號密碼(認證成功後，會生成Jwt token，並且看blacklistRepository中，有沒有此帳號的人在裡面，有就刪除此資料，沒有就回傳token)
	@Transactional
	public String login(String userAccount, String userPassword) {
		boolean loginSuccessful = authenticateUser(userAccount, userPassword);
        if (loginSuccessful) {
            String token = jwtTokenGenerator.generateToken(userAccount);
            boolean exists = blacklistRepository.existsByAccount(userAccount);
    		if(exists) {
    			blacklistRepository.deleteByAccount(userAccount);
    		}
            return token;
        } 
        return "error";
    }
	
	//驗證有無此使用者，以及對應輸入的密碼是否與userRepository中的密碼相同
	private boolean authenticateUser(String userAccount, String userPassword) {
        User user = userRepository.findByUserAccount(userAccount);
        if (user != null && user.getUserPassword().equals(userPassword)) {
            return true;
        }
        return false;
    }
	
	//註冊新的user(先確認輸入的兩個密碼是否相同，再確認userRepository中有沒有相同帳號名稱的user，都通過後，新增user物件，然後把輸入得資料set進去)
	public String register(String Account, String Password, String comfirmPassword,
			String Name, int Phone, String Email, int role) {
		User oldUser = userRepository.findByUserAccount(Account);
		if(!Password.equals(comfirmPassword)) {
			return "Password not match";
		}
		//不能有相同的帳號名稱
		else if(oldUser != null) {
			return "Please change account";
		}
		//密碼為4~16位數
		else if(Password.length()<4 || Password.length()>16) {
			return "Password is 4 to 16 digits";
		}
		//電話號為9-00-000-000~9-99-999-999區間
		else if(Phone < 900000000 || Phone > 999999999) {
			return "Phone form is wrong";
		}
		User user = new User();
		user.setUserAccount(Account);
		user.setUserPassword(Password);
		user.setUserName(Name);
		user.setUserPhone(Phone);
		user.setUserEmail(Email);
		user.setRole(role);
		userRepository.save(user);
		return "Success";
	}
	
	//忘記密碼(確認有沒有此mail的user，有就確認新密碼跟確認密碼是否相同)
	public String forget(String userEmail, String Password, String comfirmPassword) {
		User user = userRepository.findByUserEmail(userEmail);
		if(user == null) {
			return "Not found this mail";
		}
		if(!Password.equals(comfirmPassword)) {
			return "Password not match";
		}
		user.setUserPassword(Password);
		userRepository.save(user);
		return "Success";
	}
	
	//登出(如果此帳號不存在blacklistRepository中，把此帳號的名稱，丟入黑名單中)
	public String logout(String userAccount) {
        boolean exists = blacklistRepository.existsByAccount(userAccount);
        User user = userRepository.findByUserAccount(userAccount);
		if(!exists) {
			Blacklist Buser = new Blacklist();
			Buser.setAccount(user.getUserAccount());
	        blacklistRepository.save(Buser);
	        return "success";
		}
		return "false";
	}

	//是不是admin
	public boolean hasAdminRole(String userAccount) {
	    User user = userRepository.findByUserAccount(userAccount);
	    return user != null && (user.getRole() == 1);
	}
	
	//是不是user
	public boolean hasUserRole(String userAccount) {
	    User user = userRepository.findByUserAccount(userAccount);
	    return user != null && (user.getRole() == 0);
	}
}
