class Console: IOutputInfo{

    override fun showMessage(msg: String, lineBreak: Boolean){
        if (lineBreak){ println(msg) } else { print(msg) }
    }

    override fun show(userList: List<UserEntity>, msg: String){
        if (userList.isNotEmpty()){
            showMessage(msg, true)
            userList.forEachIndexed{index, user ->
                showMessage("\t${index + 1}: $user", true)
            }
        }else { showMessage("No users found.", true)}
    }
}