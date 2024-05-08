interface IOutputInfo {

    fun showMessage(msg: String, lineBreak: Boolean = true)
    fun show(userList: List<UserEntity>, msg: String = "All users: ")
}