package com.mungkive.application.ui.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mungkive.application.R
import com.mungkive.application.viewmodels.ApiTestViewModel

@Composable
fun RegisterView(
    modifier: Modifier = Modifier,
    viewModel: ApiTestViewModel,
    onRegisterSuccess: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel::clearIdAndPw
    }

    var passwordText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // 로고
        Image(
            painter = painterResource(R.drawable.dummylogo),
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 환영 문구
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("멍카이브")
                }
                append("에 오신 것을\n환영합니다!")
            },
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 아이디 입력
        OutlinedTextField(
            value = viewModel.id,
            onValueChange = viewModel::onIdChange,
            label = { Text("아이디") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .defaultMinSize(minHeight = 56.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 입력
        OutlinedTextField(
            value = viewModel.pw,
            onValueChange = viewModel::onPwChange,
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .defaultMinSize(minHeight = 56.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 재입력
        OutlinedTextField(
            value = passwordText,
            onValueChange = { passwordText = it },
            label = { Text("비밀번호 재입력") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .defaultMinSize(minHeight = 56.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (viewModel.pw.isNotEmpty() && viewModel.pw != passwordText) {
                "비밀번호가 일치하지 않습니다"
            } else {
                ""
            },
            fontSize = 12.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth(0.85f),
            color = Color.Red
        )

        Spacer(modifier = Modifier.weight(1f))

        // 버튼들
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    viewModel.register() { success ->
                        if (success) {
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3378F6),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp),
                enabled = (viewModel.id.isNotEmpty()
                        && viewModel.pw.isNotEmpty() && viewModel.pw == passwordText)
            ) {
                Text(text = "가입하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
